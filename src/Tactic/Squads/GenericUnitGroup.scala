package Tactic.Squads

import ProxyBwapi.UnitInfo.UnitInfo

case class GenericUnitGroup(override val groupUnits: Seq[UnitInfo]) extends UnitGroup {
  def this(unit: UnitInfo) {
    this(Seq(unit))
  }
}