package Mathematics.Physics

import Debugging.Visualizations.{ForceLabel, ForceMap}
import Mathematics.Points.Pixel
import Mathematics.PurpleMath

object ForceMath {

  def sumAll(forces: Force*): Force = sum(forces)
  def sum(forces: Traversable[Force]): Force = {
    // Loop unwound in an attempt to spare performance
    var x: Double = 0d
    var y: Double = 0d
    forces.foreach(f => {
      x += f.x
      y += f.y
    })
    Force(x, y)
  }
  
  def fromPixels(from: Pixel, to: Pixel, magnitude: Double = 1.0): Force = if (from == to) new Force() else fromRadians(from.radiansTo(to), magnitude)
  
  def fromRadians(radians: Double, magnitude: Double = 1.0): Force =
    Force(
      magnitude * Math.cos(radians),
      magnitude * Math.sin(radians))

  def rebalance(forceMap: ForceMap, scale: Double, forces: ForceLabel*): Unit = {
    val length = ForceMath.sum(forces.map(forceMap)).lengthFast
    forces.foreach(f => forceMap(f) *= PurpleMath.nanToZero(scale / length))
  }
}
