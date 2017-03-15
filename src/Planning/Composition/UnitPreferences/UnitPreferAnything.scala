package Planning.Composition.UnitPreferences
import BWMirrorProxy.UnitInfo.FriendlyUnitInfo

object UnitPreferAnything extends UnitPreference {
  override def preference(unit: FriendlyUnitInfo): Double = 0
}
