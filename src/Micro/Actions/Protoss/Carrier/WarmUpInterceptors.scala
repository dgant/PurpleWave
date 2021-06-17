package Micro.Actions.Protoss.Carrier

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}


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
      Maff.maxBy(unit.zone.units.filter(u => isLegal(u) && unit.inRangeToAttack(u)))(_.totalHealth)
      .orElse(Maff.minBy(unit.zone.units.filter(u => isLegal(u)))(_.pixelDistanceCenter(unit)))
    Commander.attack(unit)
  }
}
