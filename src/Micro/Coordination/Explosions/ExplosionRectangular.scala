package Micro.Coordination.Explosions

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

abstract class ExplosionRectangular extends Explosion {
  
  def start: Pixel
  def end: Pixel
  
  override def draw(): Unit = {
    DrawMap.box(start, end, color)
  }
  
  def pixelsOfEntanglement(unit: UnitInfo): Double = {
    -unit.pixelDistanceEdge(start, end)
  }
  
  override def directionTo(unit: UnitInfo): Force = {
    new Force(unit.pixelCenter.subtract(start.midpoint(end)))
  }
}
