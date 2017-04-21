package Information.Battles.Simulation.Construction

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Pixels.Pixel

abstract class BattleSimulationEvent(
  val frame : Int,
  val unit  : Simulacrum) {
  
  val description:String
  def draw()
  override def toString: String = description.replaceAllLiterally("Pixel", "")
}

class BattleSimulationEventFlee(
  argFrame  : Int,
  argUnit   : Simulacrum)
  extends BattleSimulationEvent(argFrame, argUnit) {
  
  val description = frame + ": " + unit + " decides to flee"
  
  override def draw = {}
}

class BattleSimulationEventFight(
  argFrame  : Int,
  argUnit   : Simulacrum)
  extends BattleSimulationEvent(argFrame, argUnit) {
  
  val description = frame + ": " + unit + " decides to fight"
  
  override def draw = {}
}

class BattleSimulationEventMoveTowards(
      argFrame  : Int,
      argUnit   : Simulacrum,
  val from      : Pixel,
  val to        : Pixel,
  val towards   : Pixel)
    extends BattleSimulationEvent(argFrame, argUnit) {
  
  val description = frame + ": " + unit + " charges from " + from + " to " + to + " towards " + towards
  
  override def draw = DrawMap.arrow(from, to, unit.unit.player.colorNeon)
}

class BattleSimulationEventMoveAway(
      argFrame  : Int,
      argUnit   : Simulacrum,
  val from      : Pixel,
  val to        : Pixel,
  val towards   : Pixel)
  extends BattleSimulationEvent(argFrame, argUnit) {
  
  val description = frame + ": " + unit + " flees from " + from + " to " + to + " towards " + towards
  
  override def draw = DrawMap.arrow(from, to, unit.unit.player.colorDark)
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

