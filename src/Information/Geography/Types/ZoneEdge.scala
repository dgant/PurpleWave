package Information.Geography.Types

import Mathematics.Pixels.Pixel
import bwta.Chokepoint

class ZoneEdge(
  chokepoint: Chokepoint,
  val zones:Iterable[Zone]) {
  
  val centerPixel   = new Pixel(chokepoint.getCenter)
  val radiusPixels  = chokepoint.getWidth / 2
  val sidePixels    = Vector(chokepoint.getSides.first, chokepoint.getSides.second)
  
  def contains(pixel:Pixel):Boolean = centerPixel.pixelDistanceFast(pixel) <= radiusPixels
}