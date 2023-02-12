package Mathematics.Physics

import Mathematics.Points.Pixel

case class Gravity(pixel: Pixel, magnitude: Double) {
  def apply(target: Pixel): Force = {
    val scale = magnitude / target.pixelDistanceSquared(pixel)
    Force(
      scale * (pixel.x - target.x),
      scale * (pixel.y - target.y))
  }
}