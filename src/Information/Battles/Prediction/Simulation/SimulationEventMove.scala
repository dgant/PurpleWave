package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.Pixel

case class SimulationEventMove(
  frame   : Int,
  sim     : Simulacrum,
  from    : Pixel,
  to      : Pixel,
  frames  : Int)
    extends SimulationEvent {
  
  override def toString: String = Vector(
    frame + ":",
    describe(sim),
    "moves from",
    from,
    "to",
    to,
    "in",
    frames,
    "frames"
    ).map(_.toString).mkString(" ")
  
  override def draw() {
    DrawMap.arrow(from, to, sim.realUnit.player.colorMedium)
  }
}
