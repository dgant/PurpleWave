package Mathematics.Physics

import Mathematics.Points.Pixel

object BuildForce {
  
  def fromPixels(from: Pixel, to: Pixel, magnitude: Double = 1.0): Force =
    fromRadians(from.radiansTo(to), magnitude)
  
  def fromRadians(radians: Double, magnitude: Double): Force =
    Force(
      magnitude * Math.cos(radians),
      magnitude * Math.sin(radians))
}
