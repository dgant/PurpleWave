package Micro.Actions.Protoss

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Protoss.Carrier._
import Micro.Agency.Commander
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}
import Utilities.ByOption

object BeCarrier extends Action {
  
  // Carriers are really finicky.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.aliveAndComplete           &&
    unit.is(Protoss.Carrier)        &&
    unit.matchups.enemies.nonEmpty
  }

  protected final def interceptorActive(interceptor: UnitInfo): Boolean = (
    ! interceptor.complete
    || interceptor.order == Orders.InterceptorAttack
    || With.framesSince(interceptor.lastFrameStartingAttack) < 72
  )
  override protected def perform(unit: FriendlyUnitInfo) {

    def airToAirSupply(units: Seq[UnitInfo]): Double = {
      units.map(u => if (u.flying) u.unitClass.supplyRequired else 0).sum
    }
    def shouldNeverHitUs(threat: UnitInfo): Boolean = {
      // Never stand in range of static defense
      if ( ! threat.canMove) return true
      
      // We can't always run from faster flying units
      if (threat.flying && threat.topSpeed >= unit.topSpeed) return false
      
      // We can't kite Goliaths, but we should only take shots from them when launching interceptors
      // and if we can afford to take some damage
      if ( ! threat.flying && (interceptorsActive || unit.totalHealth < unit.unitClass.maxHitPoints)) return true
      if (unit.agent.shouldEngage && threat.pixelRangeAgainst(unit) > 32.0 * 6.0) return false
      
      true
    }

    lazy val framesToAccelerate = unit.framesToAccelerate
    lazy val interceptorsDone   = unit.interceptors.filter(_.complete)
    lazy val interceptorsActive = interceptorsDone.count(interceptorActive) >= interceptorsDone.size / 2
    lazy val canLeave           = airToAirSupply(unit.matchups.threats) < airToAirSupply(unit.matchups.alliesInclSelf) || unit.matchups.threats.exists(_.isAny(Terran.Battlecruiser, Protoss.Carrier))
    lazy val exitingLeash       = unit.matchups.targets.filter(_.matchups.framesToLive > 48).forall(_.pixelDistanceEdge(unit) > 32.0 * 7.0)
    lazy val inRangeNeedlessly  = unit.matchups.threats.exists(threat => shouldNeverHitUs(threat) && unit.pixelsOfEntanglement(threat) > - 32)
    lazy val safeFromThreats    = unit.matchups.threats.forall(threat => threat.pixelDistanceCenter(unit) > threat.pixelRangeAir + 8 * 32 && ! threat.is(Protoss.Carrier)) // Protect interceptors!
    lazy val shouldFight        = interceptorsDone.nonEmpty && ! inRangeNeedlessly && (unit.agent.shouldEngage || safeFromThreats || (interceptorsDone.nonEmpty && ! canLeave))

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
        Target.choose(unit)
        if (safeFromThreats && interceptorsActive) {
          CarrierChase.consider(unit)
        }
        Commander.attack(unit)
      }
      WarmUpInterceptors.consider(unit)
      if (unit.matchups.targets.exists(_.isAny(Protoss.Carrier, Protoss.Interceptor))) {
        unit.agent.toAttack = unit.agent.toAttack
          .orElse(ByOption.minBy(unit.matchups.targetsInRange .filter(u => u.canAttack(unit) && ! u.is(Protoss.Interceptor)))(u => u.totalHealth / (1 + u.subjectiveValue)))
          .orElse(ByOption.minBy(unit.matchups.targets        .filter(u => u.canAttack(unit) && ! u.is(Protoss.Interceptor)))(_.pixelDistanceEdge(unit)))
        Commander.attack(unit)
      }
      Commander.attackMove(unit)
    } else {
      Retreat.consider(unit)
    }
  }
}
