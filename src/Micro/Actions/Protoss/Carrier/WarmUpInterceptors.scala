package Micro.Actions.Protoss.Carrier

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}
import Utilities.ByOption

object WarmUpInterceptors extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.interceptors.exists(i => i.complete && i.order == Orders.Nothing)
    && unit.matchups.targets.isEmpty
    && unit.pixelDistanceCenter(unit.agent.destination) < 32.0 * 5.0
  )

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    def isLegal(target: UnitInfo): Boolean = (
      unit.canAttack(target)
      && target.totalHealth > Protoss.Zealot.maxTotalHealth - 10
      && ! target.is(Protoss.Carrier)
    )
    unit.agent.toAttack =
      ByOption.maxBy(unit.zone.units.filter(u => isLegal(u) && unit.inRangeToAttack(u)))(_.totalHealth)
      .orElse(ByOption.minBy(unit.zone.units.filter(u => isLegal(u)))(_.pixelDistanceCenter(unit)))
    Attack.delegate(unit)
  }
}
