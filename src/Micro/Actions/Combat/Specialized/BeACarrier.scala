package Micro.Actions.Combat.Specialized

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.TargetRelevant
import Micro.Actions.Combat.Maneuvering.HoverOutsideRange
import Micro.Actions.Commands.Attack
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders}

object BeACarrier extends Action {
  
  // Carriers are really finicky.
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.aliveAndComplete           &&
    unit.is(Protoss.Carrier)        &&
    unit.matchups.targets.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    lazy val interceptorsTotal        = unit.interceptors.count(_.aliveAndComplete)
    lazy val interceptorsFighting     = unit.interceptors.count(_.order == Orders.InterceptorAttack)
    lazy val interceptorsAreShooting  = interceptorsFighting * 2 >= interceptorsTotal
    lazy val exitingLeash             = unit.matchups.targets.forall(_.pixelDistanceFast(unit) > 32.0 * 9.0)
    
    if (interceptorsTotal > 2 && (exitingLeash || ! interceptorsAreShooting)) {
      TargetRelevant.delegate(unit)
      Attack.consider(unit)
    }
    else {
      HoverOutsideRange.consider(unit)
    }
  }
}
