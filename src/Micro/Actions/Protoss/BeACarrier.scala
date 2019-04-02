package Micro.Actions.Protoss

import Micro.Actions.Action
import Micro.Actions.Commands.{Attack, AttackMove}
import Micro.Actions.Protoss.Carrier._
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}

object BeACarrier extends Action {
  
  // Carriers are really finicky.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.aliveAndComplete           &&
    unit.is(Protoss.Carrier)        &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    def interceptorActive(interceptor: UnitInfo): Boolean = {
      ! unit.complete || (unit.order == Orders.InterceptorAttack)
    }
    def airToAirSupply(units: Seq[UnitInfo]): Double = {
      units.map(u => if (u.flying) u.unitClass.supplyRequired else 0).sum
    }
    def shouldNeverHitUs(threat: UnitInfo): Boolean = {
      // Never stand in range of static defense
      if ( ! threat.canMove) return true
      
      // We can't always run from faster flying units
      if (threat.flying && threat.topSpeed >= unit.topSpeed)  return false
      
      // We can't kite Goliaths, but we should only take shots from them when launching interceptors
      // and if we can afford to take some damage
      if ( ! threat.flying && (interceptorsActive || unit.totalHealth < unit.unitClass.maxHitPoints)) return true
      if (unit.agent.shouldEngage && threat.pixelRangeAgainst(unit) > 32.0 * 6.0) return false
      
      true
    }

    lazy val framesToAccelerate = unit.framesToAccelerate
    lazy val interceptors       = unit.interceptors.count(_.complete)
    lazy val canLeave           = airToAirSupply(unit.matchups.threats) < airToAirSupply(unit.matchups.alliesInclSelf)
    lazy val exitingLeash       = unit.matchups.targets.filter(_.matchups.framesToLive > 48).forall(_.pixelDistanceEdge(unit) > 32.0 * 7.0)
    lazy val inRangeNeedlessly  = unit.matchups.threats.exists(threat => shouldNeverHitUs(threat) && unit.matchups.framesOfEntanglementWith(threat) > - Math.max(unit.framesToTurnFrom(threat), unit.framesToAccelerate))
    lazy val safeFromThreats    = unit.matchups.threats.forall(threat => threat.pixelDistanceCenter(unit) > threat.pixelRangeAir + 8 * 32) // Protect interceptors!
    lazy val shouldFight        = interceptors > 0 && ! inRangeNeedlessly && (unit.agent.shouldEngage || safeFromThreats  || (interceptors > 1 && ! canLeave))
    lazy val interceptorsActive = unit.interceptors.count(interceptorActive) >= unit.interceptors.size / 2

    if (shouldFight) {
      // Avoid changing targets (causes interceptors to not attack)
      // Avoid targeting something leaving leash range
      // Keep moving/reposition while
      val targetNow = unit.orderTarget.filter(t => t.alive && t.visible && t.pixelDistanceEdge(unit) < 32.0 * 10.0)
      val targetDistance = targetNow.map(unit.pixelDistanceEdge)
      if (safeFromThreats && targetDistance.exists(_ < 32.0 * 9.5)) {
        unit.agent.toAttack = targetNow
        if (unit.interceptors.forall(interceptorActive) || targetDistance.exists(_ < 32.0 * 8.0)) {
          CarrierOpenLeash.consider(unit)
        } else {
          CarrierHoldLeash.consider(unit)
        }
      }
      else {
        CarrierTarget.consider(unit)
        if (safeFromThreats && interceptorsActive) {
          CarrierChase.consider(unit)
        }
        Attack.consider(unit)
      }
      WarmUpInterceptors.consider(unit)
      AttackMove.consider(unit)
    }
    else {
      CarrierRetreat.consider(unit)
    }
  }
}
