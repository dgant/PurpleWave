package Micro.Coordination.Explosions

import Debugging.Visualizations.Colors
import Mathematics.Physics.Force
import ProxyBwapi.UnitInfo.UnitInfo

trait Explosion {
  
  protected val color = Colors.BrightYellow
  
  def draw(): Unit
  def affects(unit: UnitInfo): Boolean
  def framesRemaining: Double
  def pixelsOfEntanglement(unit: UnitInfo): Double
  def directionTo(unit: UnitInfo): Force
}
