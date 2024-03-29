package Micro.Actions.Protoss.Carrier

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Orders
import ProxyBwapi.UnitInfo.FriendlyUnitInfo


object RelaxInterceptors extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.interceptors.forall(_.order == Orders.InterceptorAttack)
    && unit.matchups.targets.isEmpty
    && unit.matchups.threats.isEmpty
  )

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val friendlyTargets = unit.interceptors.flatMap(_.orderTarget.filter(_.isFriendly))
    val nearestTarget = Maff.minBy(friendlyTargets)(_.pixelDistanceCenter(unit))
    nearestTarget.foreach(t => {
      unit.agent.decision.set(t.pixel.project(unit.pixel, 32 * 12))
      Commander.move(unit)
    })
  }
}
