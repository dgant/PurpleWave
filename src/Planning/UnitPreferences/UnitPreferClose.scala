package Planning.UnitPreferences

import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class UnitPreferClose(pixel: Pixel = SpecificPoints.middle) extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double = (
    (if (With.performance.danger)
      unit.pixelDistanceCenter(pixel)
    else
      unit.framesToTravelTo(pixel))
    * (if (unit.carryingMinerals) 1.2   else 1.0)
    * (if (unit.carryingGas)      1.25  else 1.0))
}
