package Information.Battles.Types

import Information.Battles.Prediction.LocalBattleMetrics
import Lifecycle.With
import Mathematics.PurpleMath

class NewBattleJudgment(battle: BattleLocal) {
  val modifiers = JudgmentModifiers(battle)

  val scoreFinal  : Double = transformTotalScore(battle.predictionAttack.localBattleMetrics)
  val scoreTarget : Double = 0.0
  val shouldFight : Boolean = scoreFinal > scoreTarget
  val confidence  : Double = PurpleMath.nanToN((scoreFinal - scoreTarget) / Math.abs(Math.signum(scoreFinal - scoreTarget) - scoreTarget), if (scoreFinal >= scoreTarget) 1 else -1)

  def transformTotalScore(metrics: Seq[LocalBattleMetrics]): Double = {
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
}
