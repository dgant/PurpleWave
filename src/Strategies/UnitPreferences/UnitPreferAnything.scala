package Strategies.UnitPreferences
import Types.UnitInfo.FriendlyUnitInfo

object UnitPreferAnything extends UnitPreference {
  override def preference(unit: FriendlyUnitInfo): Double = 0
}
