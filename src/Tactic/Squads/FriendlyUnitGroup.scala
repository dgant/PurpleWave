package Tactic.Squads

import ProxyBwapi.UnitInfo.UnitInfo

trait FriendlyUnitGroup extends UnitGroup with TFriendlyUnitGroup {
  def groupUnits: Seq[UnitInfo] = groupFriendlyUnits
}
