package Tactic.Squads

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class GenericFriendlyUnitGroup(override val groupFriendlyUnits: Seq[FriendlyUnitInfo]) extends FriendlyUnitGroup {
  def this(unit: FriendlyUnitInfo) {
    this(Seq(unit))
  }
}