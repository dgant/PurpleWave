package Planning.UnitPreferences
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferScout extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    if (unit.canMove) (
      - unit.topSpeed
      * (if (unit.flying) 1.5 else 1.0)
      * (if (unit.unitClass.permanentlyCloaked) 1.5 else 1.0))
    else
      Double.MaxValue
  }
}
