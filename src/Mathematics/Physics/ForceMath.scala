package Mathematics.Physics

import Mathematics.Points.Pixel
import Mathematics.PurpleMath

object ForceMath {

  def sumAll(forces: Force*): Force = sum(forces)
  def sum(forces: Traversable[Force]): Force = forces.foldLeft(new Force)(_ + _)
  
  def fromPixels(from: Pixel, to: Pixel, magnitude: Double = 1.0): Force =
    fromRadians(from.radiansTo(to), magnitude)
  
  def fromRadians(radians: Double, magnitude: Double): Force =
    Force(
      magnitude * Math.cos(radians),
      magnitude * Math.sin(radians))
}
