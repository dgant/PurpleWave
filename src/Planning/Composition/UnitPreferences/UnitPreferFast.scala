package Planning.Composition.UnitPreferences
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferFast extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    if (unit.canMove)
      - unit.topSpeed * (if (unit.flying) 1.5 else 1.0)
    else
      Double.MaxValue
  }
}
