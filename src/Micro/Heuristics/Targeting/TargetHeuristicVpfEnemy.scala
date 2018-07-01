package Micro.Heuristics.Targeting

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object TargetHeuristicVpfEnemy extends TargetHeuristic {
  
  // Divisor to make vpfs scale on 1.0+
  // Based on the lowest VPF in the game: Arbiter against Zergling :)
  lazy val baselineVpf = Vector(
    Zerg.Zergling.subjectiveValue * 0.5 * Protoss.Arbiter.effectiveGroundDamage / Protoss.Arbiter.groundDamageCooldown,
    With.economy.incomePerFrameMinerals,
    With.economy.incomePerFrameGas * MicroValue.gasToMineralsRatio).min
  
  private val incompleteCompletionHorizon = 24 * 5
  protected def incompleteBuildingVpf(unit: UnitInfo): Double = {
    val dpfAir    = PurpleMath.nanToZero(unit.unitClass.effectiveAirDamage / unit.unitClass.airDamageCooldown)
    val dpfGround = PurpleMath.nanToZero(unit.unitClass.effectiveGroundDamage / unit.unitClass.groundDamageCooldown)
    val vpfAir    = dpfAir    * ByOption.max(unit.matchups.enemies.filter(   _.flying).map(_.subjectiveValue)).getOrElse(0.0)
    val vpfGround = dpfGround * ByOption.max(unit.matchups.enemies.filter( ! _.flying).map(_.subjectiveValue)).getOrElse(0.0)
    val discount  = Math.max(0.0, (incompleteCompletionHorizon - unit.remainingCompletionFrames) / incompleteCompletionHorizon)
    val output    = Math.max(vpfAir, vpfGround) * discount
    output
  }
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    val numerator =
      if (candidate.gathering || candidate.base.exists(_.harvestingArea.contains(candidate.tileIncludingCenter))) {
        With.economy.incomePerFrameMinerals
      }
      else if (candidate.repairing && candidate.target.isDefined) {
        MicroValue.valuePerFrameRepairing(candidate.target.get)
      }
      else if (candidate.constructing && candidate.target.isDefined) {
        Math.max(
          incompleteBuildingVpf(candidate.target.get),
          candidate.target.get.subjectiveValue / candidate.target.get.unitClass.buildFrames)
      }
      else if (candidate.unitClass.isBuilding && ! candidate.complete) {
        incompleteBuildingVpf(candidate)
      }
      else {
        val vpfNow = candidate.matchups.vpfDealingInRange
        val vpfMax = candidate.matchups.vpfDealingMax
        vpfNow + 0.1 * vpfMax
      }
    
    val output = numerator / baselineVpf
    output
  }
  
}
