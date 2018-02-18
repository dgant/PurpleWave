package Micro.Actions.Protoss

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Maneuvering.{CliffAvoid, OldAvoid}
import Micro.Actions.Commands.AttackMove
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
    lazy val interceptorActiveOrders  = Array(Orders.InterceptorAttack, Orders.InterceptorReturn)
    lazy val interceptorsTotal        = unit.interceptors.count(_.aliveAndComplete)
    lazy val interceptorsFighting     = unit.interceptors.count(i => interceptorActiveOrders.contains(i.order))
    lazy val interceptorsAreShooting  = interceptorsFighting >= interceptorsTotal - 1
    lazy val exitingLeash             = unit.matchups.targets.filter(_.matchups.framesToLiveDiffused > GameTime(0, 2)()).forall(_.pixelDistanceEdge(unit) > 32.0 * 7.0)
    lazy val interceptorsNeedKick     = interceptorsTotal > 0 && (exitingLeash || ! interceptorsAreShooting)
    
    def threatUnacceptable(threat: UnitInfo): Boolean = {
      if (threat.flying)                            return false
      if (threat.damageOnNextHitAgainst(unit) < 5)  return false
      if (interceptorsTotal > 2 && threat.pixelDistanceEdge(unit) < unit.pixelRangeAgainst(threat) - 32.0) return false
      true
    }
    
    val unacceptableThreat = unit.matchups.threatsInRange.find(threatUnacceptable)
    if (unacceptableThreat.isDefined) {
      CliffAvoid.consider(unit)
      OldAvoid.consider(unit)
    }
    else if (unit.matchups.targets.nonEmpty && interceptorsNeedKick && (unit.matchups.threats.isEmpty || interceptorsTotal > 2)) {
      Target.consider(unit)
      if (unit.agent.toAttack.isDefined) {
        val attackPoint = unit.agent.toAttack.get.pixelCenter.project(unit.pixelCenter, 7.5 * 32.0)
        unit.agent.toTravel = Some(attackPoint)
      }
      AttackMove.consider(unit)
    }
    else {
      CliffAvoid.consider(unit)
      OldAvoid.consider(unit)
    }
  }
}
