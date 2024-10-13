package Information.Battles.Types

import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?

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
  val confidence01Total   : Double  = confidence11Total   / 2 + 0.5
  val confidence01Air     : Double  = confidence11Air     / 2 + 0.5
  val confidence01Ground  : Double  = confidence11Ground  / 2 + 0.5

  private def weigh(skimUs: => Double, skimEnemy: => Double): Double = {
    lazy val scoreSkim = Maff.nanToOne((skimUs - skimEnemy) / (skimUs + skimEnemy))
    scoreSim11 * battle.simWeight + scoreSkim * battle.skimWeight
  }

  private def calculateSimulationScore11: Double = {
    if ( ! battle.simulated) return 0.0
    if (battle.simulationCheckpoints.lastOption.forall(_.healthValueLostUs <= 0)) return 1.0 // This can happen when all simulated enemies run away and nobody does any damage

    Maff.weightedMean(battle.simulationCheckpoints.view.map(m => (m.totalScore, m.cumulativeTotalDecisiveness)))
  }

  private def calculateTarget: Double = {
    Maff.clamp11(battle.judgmentModifiers.view.map(_.targetDelta).sum)
  }

  private def calculateConfidence11(score: Double, target: Double): Double = {
    Maff.nanToN((score - target) / Math.abs(Math.signum(score - target) - target), ?(score >= target, 1, -1))
  }

  def unitShouldFight(unit: UnitInfo): Boolean = ?(unit.flying, shouldFightAir, shouldFightGround)
}
