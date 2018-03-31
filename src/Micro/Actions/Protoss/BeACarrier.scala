package Micro.Actions.Protoss

import Micro.Actions.Action
import Micro.Actions.Basic.Pass
import Micro.Actions.Combat.Attacking.Filters.TargetFilter
import Micro.Actions.Combat.Attacking.TargetAction
import Micro.Actions.Combat.Maneuvering.CliffAvoid
import Micro.Actions.Commands.{Attack, AttackMove}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object BeACarrier extends Action {
  
  // Carriers are really finicky.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.aliveAndComplete           &&
    unit.is(Protoss.Carrier)        &&
    unit.matchups.enemies.nonEmpty
  }
  
  object CarrierTargetFilterIgnoreInterceptors extends TargetFilter {
    override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean =
      ! target.is(Protoss.Interceptor)
  }
  
  object CarrierTargetFilterShootsUp extends TargetFilter {
    override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean =
      target.canAttack(actor)
  }
  
  object CarrierTargetFilterInRange extends TargetFilter {
    override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean =
      (target.pixelDistanceEdge(actor) < 32.0 * 8.0)
  }
  
  object CarrierTargetFilterInLeash extends TargetFilter {
    override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean =
      target.pixelDistanceEdge(actor) < 32.0 * 10.0 && actor.interceptors.exists(_.pixelCenter != actor.pixelCenter)
  }
  
  object CarrierTarget extends TargetAction(CarrierTargetFilterIgnoreInterceptors) {
    override val additionalFiltersOptional = Vector(
      CarrierTargetFilterShootsUp,
      CarrierTargetFilterInRange,
      CarrierTargetFilterInLeash
    )
  }
  
  object WarmUpInterceptors extends Action {
    override def allowed(unit: FriendlyUnitInfo): Boolean = (
      unit.interceptors.nonEmpty
      && unit.matchups.targets.isEmpty
      && unit.pixelDistanceCenter(unit.agent.destination) < 32.0 * 5.0
    )
  
    override protected def perform(unit: FriendlyUnitInfo): Unit = {
      def isLegal(target: UnitInfo): Boolean = (
        unit.canAttack(target)
        && target.totalHealth > Protoss.Zealot.maxTotalHealth - 10
      )
      unit.agent.toAttack =
        ByOption.maxBy(unit.zone.units.toVector.filter(u => isLegal(u) && unit.inRangeToAttack(u)))(_.totalHealth)
        .orElse(ByOption.minBy(unit.zone.units.toVector.filter(u => isLegal(u)))(_.pixelDistanceCenter(unit)))
      Attack.delegate(unit)
    }
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    def airToAirSupply(units: Seq[UnitInfo]): Double = {
      units.map(u => if (u.flying) u.unitClass.supplyRequired else 0).sum
    }
    def shouldNeverHitUs(threat: UnitInfo): Boolean = {
      if ( ! threat.canMove)                        return true
      if (threat.flying)                            return false
      if (threat.damageOnNextHitAgainst(unit) < 5)  return false
      if (interceptorsTotal > 2 && threat.pixelDistanceEdge(unit) < unit.pixelRangeAgainst(threat) - 32.0) return false
      true
    }
    
    lazy val canLeave                 = airToAirSupply(unit.matchups.threats) < airToAirSupply(unit.matchups.alliesInclSelf)
    lazy val exitingLeash             = unit.matchups.targets.filter(_.matchups.framesToLive > 48).forall(_.pixelDistanceEdge(unit) > 32.0 * 7.0)
    lazy val inRangeNeedlessly        = unit.matchups.threatsInRange.exists(shouldNeverHitUs)
    lazy val interceptorsTotal        = unit.interceptors.count(_.aliveAndComplete)
    lazy val interceptorsFighting     = unit.interceptors.count(_.pixelCenter != unit.pixelCenter)
    lazy val interceptorsAreShooting  = interceptorsFighting >= interceptorsTotal - 1
    lazy val interceptorsNeedKick     = interceptorsTotal > 0 && (exitingLeash || ! interceptorsAreShooting)
    lazy val safeHere                 = unit.matchups.framesOfSafety >= 0 || unit.totalHealth > 150
    lazy val happyFightingHere        = unit.agent.shouldEngage && ! inRangeNeedlessly && safeHere
    lazy val shouldFight              = safeHere || (interceptorsTotal > 1 && ! canLeave)
    
    if (shouldFight) {
      // Avoid changing targets (causes interceptors to not attack)
      unit.agent.toAttack = unit.target.filter(t => t.alive && t.visible && t.pixelDistanceEdge(unit) < 32.0 * 10.0)
      CarrierTarget.consider(unit)
      
      if (unit.agent.toAttack.isDefined) {
        val target = unit.agent.toAttack.get
        if (unit.target.contains(target)) {
          Pass.consider(unit)
        }
        else if (target.unitClass.maxHitPoints > 60 || ! unit.inRangeToAttack(target)) {
          Attack.consider(unit)
        }
        else {
          unit.agent.toTravel = Some(target.pixelCenter)
        }
      }
      WarmUpInterceptors.consider(unit)
      AttackMove.consider(unit)
    }
    else {
      CliffAvoid.consider(unit)
    }
  }
}
