package Planning.Composition.UnitPreferences

import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class UnitPreferClose(pixel: Pixel = SpecificPoints.middle) extends UnitPreference {
  
  // For performance we use air distance
  override def preference(unit: FriendlyUnitInfo): Double = unit.framesToTravelPixels(unit.pixelDistanceFast(pixel))
}
