package Information.Battles.Simulation.Construction

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Pixels.Pixel

abstract class BattleSimulationEvent(
  frame : Int,
  unit  : Simulacrum) {
  
  val description:String
  override def toString: String = description
  def draw()
}

class BattleSimulationEventMove(
      argFrame  : Int,
      argUnit   : Simulacrum,
  val from      : Pixel,
  val to        : Pixel)
    extends BattleSimulationEvent(argFrame, argUnit) {
  
  val description = argUnit.toString + " moves to " + to
  
  override def draw = DrawMap.arrow(from, to, Colors.MediumGray)
}

class BattleSimulationEventAttacks(
      argFrame  : Int,
      argUnit   : Simulacrum,
  val target    : Simulacrum)
  extends BattleSimulationEvent(argFrame, argUnit) {
  
  val description = argUnit.toString + " attacks " + target
  
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
  val description = argUnit.toString + " targets " + target
  
  override def draw = DrawMap.arrow(from, to, Colors.DarkRed)
}

class BattleSimulationEventFears(
      argFrame  : Int,
      argUnit   : Simulacrum,
  val threat    : Simulacrum)
    extends BattleSimulationEvent(argFrame, argUnit) {
  
  val from = argUnit.pixel
  val to = threat.pixel
  val description = argUnit.toString + " fears " + threat
  
  override def draw = DrawMap.arrow(from, to, Colors.DarkBlue)
}

