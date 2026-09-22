package Mathematics.Physics

import Mathematics.Points.Pixel

case class Gravity(pixel: Pixel, magnitude: Double) {
  def apply(target: Pixel): Force = {
    val distanceSquared = target.pixelDistanceSquared(pixel)
    if (distanceSquared == 0) return new Force()
    val scale = magnitude / distanceSquared
    Force(
      scale * (pixel.x - target.x),
      scale * (pixel.y - target.y))
  }
}