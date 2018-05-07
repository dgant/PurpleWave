package Micro.Actions.Protoss

import Micro.Actions.Action
import Micro.Actions.Basic.Pass
import Micro.Actions.Combat.Attacking.Filters.TargetFilter
import Micro.Actions.Combat.Attacking.TargetAction
import Micro.Actions.Combat.Maneuvering.CliffAvoid
import Micro.Actions.Commands.{Attack, AttackMove}
import Planning.Yolo
import ProxyBwapi.Races.{Protoss, Terran}
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
    override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
      lazy val isRepairer = (
        target.is(Terran.SCV)
        && target.matchups.allies.exists(t => t.canAttack(actor) && target.pixelDistanceEdge(t) < 32))
  
      // Anything that can hit us or our interceptors
      val inRange = target.pixelDistanceEdge(actor) < target.pixelRangeAir + 32.0 * 8.0
  
      inRange && (target.canAttack(actor) || isRepairer)
    }
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
      CarrierTargetFilterInRange,
      CarrierTargetFilterInLeash,
      CarrierTargetFilterShootsUp
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
      // Never stand in range of static defense
      if ( ! threat.canMove) return true
      
      // We can't always run from faster flying units
      if (threat.flying && threat.topSpeed >= unit.topSpeed)  return false
      
      // We can't kite Goliaths, but we should only take shots from them when we actually want to fight
      if (unit.agent.shouldEngage
        && unit.interceptorCount > 2
        && threat.pixelRangeAgainst(unit) > 32.0 * 6.0 ) return false
      
      true
    }
    
    lazy val canLeave           = airToAirSupply(unit.matchups.threats) < airToAirSupply(unit.matchups.alliesInclSelf)
    lazy val exitingLeash       = unit.matchups.targets.filter(_.matchups.framesToLive > 48).forall(_.pixelDistanceEdge(unit) > 32.0 * 7.0)
    lazy val inRangeNeedlessly  = unit.matchups.threatsInRange.exists(shouldNeverHitUs)
    lazy val safeHere           = unit.matchups.framesOfSafety >= 0 || unit.totalHealth > 150
    lazy val happyFightingHere  = unit.agent.shouldEngage && ! inRangeNeedlessly && safeHere
    lazy val shouldFight        = unit.interceptorCount > 0 && (Yolo.active || happyFightingHere || (unit.interceptorCount > 1 && ! canLeave))
    
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
