package Information.Battles.Simulation.Construction

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Pixels.Pixel

abstract class BattleSimulationEvent(
  val frame : Int,
  val unit  : Simulacrum) {
  
  val description:String
  def draw()
  override def toString: String = description
}

class BattleSimulationEventMove(
      argFrame  : Int,
      argUnit   : Simulacrum,
  val from      : Pixel,
  val to        : Pixel)
    extends BattleSimulationEvent(argFrame, argUnit) {
  
  val description = frame + ": " + unit + " moves to " + to
  
  override def draw = DrawMap.arrow(from, to, Colors.MediumGray)
}

class BattleSimulationEventAttacks(
      argFrame  : Int,
      argUnit   : Simulacrum,
  val target    : Simulacrum)
  extends BattleSimulationEvent(argFrame, argUnit) {
  
  val description = frame + ": " + unit + " attacks " + target
  
  val from = argUnit.pixel
  val to = target.pixel
  val killed = ! target.alive
  
  override def draw = {
    DrawMap.arrow(from, to, Colors.BrightRed)
    if (killed) {
      DrawMap.circle(to, 8, Colors.BrightRed)
    }
  }
}

class BattleSimulationEventTargets(
      argFrame  : Int,
      argUnit   : Simulacrum,
  val target    : Simulacrum)
    extends BattleSimulationEvent(argFrame, argUnit) {
  
  val from = argUnit.pixel
  val to = target.pixel
  val description = frame + ": " + unit + " targets " + target
  
  override def draw = DrawMap.arrow(from, to, Colors.DarkRed)
}

class BattleSimulationEventFears(
      argFrame  : Int,
      argUnit   : Simulacrum,
  val threat    : Simulacrum)
    extends BattleSimulationEvent(argFrame, argUnit) {
  
  val from = argUnit.pixel
  val to = threat.pixel
  val description = frame + ": " + unit + " fears " + threat
  
  override def draw = DrawMap.arrow(from, to, Colors.DarkBlue)
}

