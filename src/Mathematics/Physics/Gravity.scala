package Mathematics.Physics

import Mathematics.Points.Pixel

case class Gravity(pixel: Pixel, magnitude: Double) {
  
  def apply(target: Pixel): Force = new Force(target.project(pixel, magnitude / target.pixelDistanceSquared(pixel)).subtract(target))
  
}