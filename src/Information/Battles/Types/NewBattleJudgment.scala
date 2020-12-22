package Information.Battles.Types

import Information.Battles.Prediction.LocalBattleMetrics
import Lifecycle.With
import Mathematics.PurpleMath

class NewBattleJudgment(battle: BattleLocal) {
  val modifiers = JudgmentModifiers(battle)

  val finalScore: Double = transformTotalScore(battle.predictionAttack.localBattleMetrics)
  val shouldFight: Boolean = true
  val confidence: Double = 1.0

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
