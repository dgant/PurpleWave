package Micro.Coordination.Explosions

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

abstract class ExplosionRadial extends Explosion {
  
  def center: Pixel
  def radius: Double
  
  override def draw(): Unit = {
    DrawMap.circle(center, radius.toInt, color)
  }
  
  def pixelsOfEntanglement(unit: UnitInfo): Double = {
    radius - unit.pixelDistanceCenter(center)
  }
  
  override def directionTo(unit: UnitInfo): Force = {
    new Force(unit.pixelCenter.subtract(center))
  }
}
