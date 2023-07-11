package Mathematics.Physics

import Debugging.Visualizations.ForceLabel
import Mathematics.Points.Pixel
import Mathematics.Maff

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

  def mean(forces: Traversable[Force]): Force = {
    // Loop unwound in an attempt to spare performance
    var i: Int = 0
    var x: Double = 0d
    var y: Double = 0d
    forces.foreach(f => {
      i += 1
      x += f.x
      y += f.y
    })
    Force(Maff.nanToZero(x/i), Maff.nanToZero(y/i))
  }
  
  def fromPixels(from: Pixel, to: Pixel, magnitude: Double = 1.0): Force = if (from == to) Forces.None else fromRadians(from.radiansTo(to), magnitude)
  
  def fromRadians(radians: Double, magnitude: Double = 1.0): Force =
    Force(
      magnitude * Math.cos(radians),
      magnitude * Math.sin(radians))

  def rebalance(forceMap: ForceMap, scale: Double, forces: ForceLabel*): Unit = {
    val length = ForceMath.sum(forces.map(forceMap)).lengthFast
    forces.foreach(f => forceMap(f) *= Maff.nanToZero(scale / length))
  }
}
