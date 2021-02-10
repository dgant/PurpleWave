package Planning.UnitPreferences
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferScout extends Preference {
  
  override def apply(unit: FriendlyUnitInfo): Double = {
    if (unit.canMove) (
      - unit.topSpeed
      * (if (unit.flying) 1.5 else 1.0)
      * (if (unit.unitClass.permanentlyCloaked) 1.5 else 1.0))
    else
      Double.MaxValue
  }
}
