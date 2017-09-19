package Micro.Actions.Protoss

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Maneuvering.{Avoid, CliffAvoid}
import Micro.Actions.Commands.{Attack, AttackMove}
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders}

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
    lazy val exitingLeash             = unit.matchups.targets.forall(_.pixelDistanceFast(unit) > 32.0 * 8.0)
    lazy val interceptorsNeedKick     = exitingLeash || ! interceptorsAreShooting
    
    if (unit.matchups.threatsInRange.exists(threat => ! threat.flying || threat.unitClass.isStaticDefense)) {
      CliffAvoid.consider(unit)
      Avoid.consider(unit)
    }
    else if (unit.matchups.targets.nonEmpty && interceptorsNeedKick && (unit.matchups.threats.isEmpty || interceptorsTotal > 2)) {
      
      // AIIDE 2017 hack for Ximp -- focus down the Carriers
      if (unit.matchups.targets.forall(t => t.flying)) {
        val carriers = unit.matchups.targets.filterNot(_.is(Protoss.Interceptor))
        unit.agent.toAttack = EvaluateTargets.best(unit, carriers)
        Attack.consider(unit)
      }
      else {
        Target.consider(unit)
        if (unit.agent.toAttack.isDefined) {
          val attackPoint = unit.agent.toAttack.get.pixelCenter.project(unit.pixelCenter, 7.5 * 32.0)
          unit.agent.toTravel = Some(attackPoint)
        }
        AttackMove.consider(unit)
      }
    }
    else {
      CliffAvoid.consider(unit)
      Avoid.consider(unit)
    }
  }
}
