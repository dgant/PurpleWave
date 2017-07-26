package Micro.Actions.Combat.Specialized

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.KiteMove
import Micro.Actions.Commands.AttackMove
import Micro.Heuristics.Movement.EvaluatePixels
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
      val attackTarget = EvaluatePixels.best(unit.action, unit.action.movementProfile)
      unit.action.toTravel = Some(attackTarget)
      AttackMove.consider(unit)
    }
    else {
      KiteMove.consider(unit)
    }
  }
}
