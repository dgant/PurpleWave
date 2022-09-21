package Utilities.UnitPreferences

import Mathematics.Maff
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class PreferAll(preferences: Function[FriendlyUnitInfo, Double]*) extends UnitPreference {
  override def apply(unit: FriendlyUnitInfo): Double = {
    preferences.map(_(unit)).map(Maff.fastSigmoid).sum
  }
}
