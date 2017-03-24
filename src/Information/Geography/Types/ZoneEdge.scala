package Information.Geography.Types

import bwapi.Position
import bwta.Chokepoint
import Utilities.EnrichPosition._

class ZoneEdge(
  chokepoint: Chokepoint,
  val zones:Iterable[Zone]) {
  
  val centerPixel   = chokepoint.getCenter
  val radiusPixels  = chokepoint.getWidth / 2
  val sidePixels    = List(chokepoint.getSides.first, chokepoint.getSides.second)
  
  def contains(pixel:Position):Boolean = centerPixel.pixelDistance(pixel) <= radiusPixels
}