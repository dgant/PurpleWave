package Mathematics.Physics

import Mathematics.Points.Pixel
import Mathematics.PurpleMath

object ForceMath {
  
  def sum(forces: Traversable[Force]): Force = forces.foldLeft(new Force)(_ + _)
  
  def fromPixels(from: Pixel, to: Pixel, magnitude: Double = 1.0): Force =
    fromRadians(from.radiansTo(to), magnitude)
  
  def fromRadians(radians: Double, magnitude: Double): Force =
    Force(
      magnitude * Math.cos(radians),
      magnitude * Math.sin(radians))
  
  def resist(force: Force, resistance: Force): Force = {
    Force(
      PurpleMath.clamp(force.x + resistance.x, 0.0, force.x),
      PurpleMath.clamp(force.y + resistance.y, 0.0, force.y))
  }
}
