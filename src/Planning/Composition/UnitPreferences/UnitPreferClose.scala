package Planning.Composition.UnitPreferences

import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Planning.Composition.Property

class UnitPreferClose(initialPixel: Pixel = SpecificPoints.middle) extends UnitPreference {
  
  val pixel = new Property[Pixel](initialPixel)
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    unit.framesToTravel(pixel.get)
  }
}
