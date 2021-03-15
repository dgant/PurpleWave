package Micro.Agency

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object AnchorMargin {
  @inline final def base = 16

  @inline final def apply(unit: FriendlyUnitInfo): Double = {
    unit.battle.map(_.teamOf(unit).anchorMargin()).orElse(unit.squad.map(_.anchorMargin())).getOrElse(2.0 * base)
  }

  @inline final def marginOf(units: Iterable[FriendlyUnitInfo]): Double = {
    base + Math.min(32 * 5, units.view.map(_.unitClass.dimensionMax).sum / 10)
  }
}
