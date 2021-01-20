package Micro.Actions.Protoss.Carrier

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders}
import Utilities.ByOption

object RelaxInterceptors extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.interceptors.forall(_.order == Orders.InterceptorAttack)
    && unit.matchups.targets.isEmpty
    && unit.matchups.threats.isEmpty
  )

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val friendlyTargets = unit.interceptors.flatMap(_.orderTarget.filter(_.isFriendly))
    val nearestTarget = ByOption.minBy(friendlyTargets)(_.pixelDistanceCenter(unit))
    nearestTarget.foreach(t => {
      unit.agent.toTravel = Some(t.pixel.project(unit.pixel, 32 * 12))
      Commander.move(unit)
    })
  }
}
