package Planning.Composition.UnitPreferences

import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class UnitPreferClose(pixel: Pixel = SpecificPoints.middle) extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = unit.framesToTravelTo(pixel)
}
