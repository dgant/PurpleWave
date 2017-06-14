package Information.Geography.Types

import Mathematics.Points.Pixel
import bwta.Chokepoint

class ZoneEdge(
  chokepoint: Chokepoint,
  val zones:Traversable[Zone]) {
  
  val centerPixel   = new Pixel(chokepoint.getCenter)
  val radiusPixels  = chokepoint.getWidth / 2
  val sidePixels    = Vector(new Pixel(chokepoint.getSides.first), new Pixel(chokepoint.getSides.second))
  
  def contains(pixel:Pixel):Boolean = centerPixel.pixelDistanceFast(pixel) <= radiusPixels
}