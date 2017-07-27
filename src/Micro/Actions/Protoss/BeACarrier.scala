package Micro.Actions.Protoss

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.KiteMove
import Micro.Actions.Commands.AttackMove
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders}

object BeACarrier extends Action {
  
  // Carriers are really finicky.
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.aliveAndComplete           &&
    unit.is(Protoss.Carrier)        &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    lazy val interceptorsTotal        = unit.interceptors.count(_.aliveAndComplete)
    lazy val interceptorsFighting     = unit.interceptors.count(_.order == Orders.InterceptorAttack)
    lazy val interceptorsAreShooting  = interceptorsFighting >= interceptorsTotal
    lazy val exitingLeash             = unit.matchups.targets.forall(_.pixelDistanceFast(unit) > 32.0 * 8.5)
    
    if (interceptorsTotal > 2 && (exitingLeash || ! interceptorsAreShooting)) {
      val attackTarget = unit.matchups.enemies.minBy(_.pixelDistanceFast(unit))
      val attackPoint = unit.pixelCenter.project(attackTarget.pixelCenter, 32)
      unit.action.toTravel = Some(attackPoint)
      AttackMove.consider(unit)
    }
    else {
      KiteMove.consider(unit)
    }
  }
}
