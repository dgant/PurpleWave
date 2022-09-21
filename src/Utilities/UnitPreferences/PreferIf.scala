package Utilities.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

case class PreferIf(predicate: FriendlyUnitInfo => Boolean) extends UnitPreference {
  override def apply(unit: FriendlyUnitInfo): Double = ?(predicate(unit), 1, 2)
}
