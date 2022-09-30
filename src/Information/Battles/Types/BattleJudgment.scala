package Information.Battles.Types

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

class BattleJudgment(battle: Battle) {
  lazy val scoreSim11     : Double  = calculateSimulationScore11
  val scoreTotal          : Double  = weigh(battle.us.skimStrengthTotal,  battle.enemy.skimStrengthTotal)
  val scoreAir            : Double  = weigh(battle.us.skimStrengthAir,    battle.enemy.skimStrengthVsAir)
  val scoreGround         : Double  = weigh(battle.us.skimStrengthGround, battle.enemy.skimStrengthVsGround)
  val scoreTarget         : Double  = calculateTarget
  val shouldFight         : Boolean = scoreTotal  >= scoreTarget
  val shouldFightAir      : Boolean = scoreAir    >= scoreTarget || (scoreTotal >= scoreTarget && scoreTotal > scoreGround)
  val shouldFightGround   : Boolean = scoreGround >= scoreTarget || (scoreTotal >= scoreTarget && scoreTotal > scoreAir)
  val confidence11Total   : Double  = calculateConfidence11(scoreTotal, scoreTarget)
  val confidence11Air     : Double  = Math.max(confidence11Total, calculateConfidence11(scoreAir, scoreTarget))
  val confidence11Ground  : Double  = Math.max(confidence11Total, calculateConfidence11(scoreGround, scoreTarget))
  val confidence01Total   : Double  = confidence11Total   / 2 + 1
  val confidence01Air     : Double  = confidence11Air     / 2 + 1
  val confidence01Ground  : Double  = confidence11Ground  / 2 + 1

  private def weigh(skimUs: => Double, skimEnemy: => Double): Double = {
    lazy val scoreSkim = Maff.nanToOne((skimUs - skimEnemy) / (skimUs + skimEnemy))
    scoreSim11 * battle.simWeight + scoreSkim * battle.skimWeight
  }

  private def calculateSimulationScore11: Double = {
    if ( ! battle.simulated) return 0.0
    // This can happen when all simulated enemies run away and nobody does any damage
    if (battle.simulationCheckpoints.lastOption.forall(metric => metric.localHealthLostUs <= 0)) return 1.0
    val average = Maff.weightedMean(battle.simulationCheckpoints.view.map(m => (m.totalScore, m.cumulativeTotalDecisiveness)))
    Maff.clamp(With.blackboard.aggressionRatio() * (1.0 + average) - 1.0, -1.0, 1.0)
  }

  private def calculateTarget: Double = {
    Maff.clamp(battle.judgmentModifiers.view.map(_.targetDelta).sum, -0.9, 0.9)
  }

  private def calculateConfidence11(score: Double, target: Double): Double = {
    Maff.nanToN((score - target) / Math.abs(Math.signum(score - target) - target), if (score >= target) 1 else -1)
  }

  def unitShouldFight(unit: UnitInfo): Boolean = if (unit.flying) shouldFightAir else shouldFightGround
}
