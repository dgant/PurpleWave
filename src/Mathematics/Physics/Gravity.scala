package Mathematics.Physics

import Mathematics.Maff
import Mathematics.Points.Pixel

case class Gravity(pixel: Pixel, magnitude: Double) {
  def apply(target: Pixel): Force = {
    val scale = Maff.nanToZero(magnitude / target.pixelDistanceSquared(pixel))
    Force(
      scale * (pixel.x - target.x),
      scale * (pixel.y - target.y))
  }
}