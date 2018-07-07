package Planning.UnitPreferences

import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class UnitPreferClose(pixel: Pixel = SpecificPoints.middle) extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = (
    unit.framesToTravelTo(pixel)
    * (if (unit.carryingMinerals) 1.2   else 1.0)
    * (if (unit.carryingGas)      1.25  else 1.0))
}
