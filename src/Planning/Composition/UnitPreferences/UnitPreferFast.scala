package Planning.Composition.UnitPreferences
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferFast extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    unit.topSpeed * (if (unit.flying) 2.0 else 1.0)
  }
}
