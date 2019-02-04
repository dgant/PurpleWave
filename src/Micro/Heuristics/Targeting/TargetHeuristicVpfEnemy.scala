package Micro.Heuristics.Targeting

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object TargetHeuristicVpfEnemy extends TargetHeuristic {
  
  // Divisor to make vpfs scale on 1.0+
  // Based on the lowest VPF in the game: Arbiter against Zergling :)
  lazy val baselineVpf: Double = Vector(
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
    lazy val maxTeamVpf = ByOption.max(unit.matchups.enemies.view.filter(_ != candidate).map(calculate)).getOrElse(0.0)
    val baseVpf = candidate.matchups.vpfTargetHeuristic

    var bonusVpf = 0.0

    // Bunker builders are super high priority
    if (candidate.constructing && candidate.orderTarget.exists(_.is(Terran.Bunker))) {
      bonusVpf += 2.0 * maxTeamVpf
    }
    // Turret builders too, if detection is important to them
    if (candidate.constructing && candidate.orderTarget.exists(_.is(Terran.MissileTurret)) && unit.matchups.alliesInclSelfCloaked.exists(_.canAttack)) {
      bonusVpf += maxTeamVpf
    }
    if (candidate.repairing) {
      candidate.orderTarget.foreach(bonusVpf += calculate(_))
    }
    if (candidate.is(Protoss.Arbiter) && unit.matchups.allyDetectors.isEmpty) {
      bonusVpf += maxTeamVpf
    }
    if (With.blackboard.pushKiters.get && candidate.isAny(Terran.Vulture, Protoss.Dragoon)) {
      bonusVpf += baseVpf
    }

    baseVpf + bonusVpf
  }

  def calculate(candidate: UnitInfo): Double = {
    val numerator =
      if (candidate.gathering || candidate.base.exists(_.harvestingArea.contains(candidate.tileIncludingCenter))) {
        With.economy.incomePerFrameMinerals
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
