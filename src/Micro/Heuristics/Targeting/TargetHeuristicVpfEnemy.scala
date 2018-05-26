package Micro.Heuristics.Targeting

import Lifecycle.With
import Micro.Decisions.MicroValue
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicVpfEnemy extends TargetHeuristic {
  
  // Divisor to make vpfs scale on 1.0+
  // Based on the lowest DPF in the game: Arbiter against small unit :)
  lazy val baseline = 0.5 * Protoss.Arbiter.effectiveGroundDamage / Protoss.Arbiter.groundDamageCooldown
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    if (candidate.gathering) {
      return With.economy.incomePerFrameMinerals
    }
    else if (candidate.repairing && candidate.target.isDefined) {
      return MicroValue.valuePerFrameRepairing(candidate.target.get)
    }
    else if (candidate.constructing && candidate.target.isDefined) {
      return candidate.target.get.subjectiveValue / candidate.target.get.unitClass.buildFrames
    }
    
    val vpfNow = candidate.matchups.vpfDealingInRange
    val vpfMax = candidate.matchups.vpfDealingMax
    val output = MicroValue.maxSplashFactor(candidate) * (vpfMax + vpfNow) / baseline
    output
  }
  
}
