package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.Pixel

case class SimulationEventAttack(
  frame   : Int,
  shooter : Simulacrum,
  victim  : Simulacrum,
  damage  : Int,
  fatal   : Boolean)
  extends SimulationEvent {
  
  private val pixel = victim.pixel
  
  override def toString: String = Vector(
    frame + ":",
    describe(shooter),
    "@",
    shooter.pixel,
    "strikes",
    describe(victim),
    "@",
    victim.pixel,
    "for",
    damage,
    "damage",
    if (fatal) "(Fatal)" else ""
  ).map(_.toString).mkString(" ")
  
  override def draw() {
    val size = 5
    val color = victim.realUnit.player.colorBright
    DrawMap.line(pixel.add(-5, -5), pixel.add(5, 5),  color)
    DrawMap.line(pixel.add(5, -5),  pixel.add(-5, 5), color)
  }

  override val from: Pixel = shooter.pixel
  override val to: Pixel = shooter.pixel
}
