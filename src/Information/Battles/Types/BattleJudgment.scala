package Information.Battles.Types

import Lifecycle.With
import Mathematics.PurpleMath

class BattleJudgment(battle: BattleLocal) {
  val fightingBefore: Boolean = battle.us.units.count(_.friendly.exists(_.agent.shouldEngage)) > battle.us.units.count(_.friendly.exists( ! _.agent.shouldEngage))

  val scoreFinal  : Double  = calculateScore
  val scoreTarget : Double  = calculateTarget
  val shouldFight : Boolean = scoreFinal >= scoreTarget
  val confidence  : Double  = calculateConfidence

  def calculateScore: Double = {
    val metrics = battle.simulationCheckpoints

    // This can happen when all simulated enemies run away and nobody does any damage
    if (metrics.lastOption.forall(metric => metric.localHealthLostUs <= 0)) return 1.0

    val average = PurpleMath.weightedMean(metrics.view.map(m => (m.totalScore, m.cumulativeTotalDecisiveness)))
    val totalValueMultiplier = With.blackboard.aggressionRatio()
    val output = PurpleMath.clamp(
      totalValueMultiplier * (1.0 + average) - 1.0,
      -1.0,
      1.0)
    output
  }

  def calculateTarget: Double = {
    PurpleMath.clamp(battle.judgmentModifiers.view.map(_.targetDelta).sum, -1, 1)
  }

  def calculateConfidence: Double = {
    PurpleMath.nanToN((scoreFinal - scoreTarget) / Math.abs(Math.signum(scoreFinal - scoreTarget) - scoreTarget), if (scoreFinal >= scoreTarget) 1 else -1)
  }
}
