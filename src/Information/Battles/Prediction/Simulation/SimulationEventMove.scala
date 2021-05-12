package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.Pixel

final case class SimulationEventMove(sim: Simulacrum, to: Pixel, frames: Int) extends SimulationEvent(sim) {

  override def toString: String = f"$frame: ${describe(sim)} moves ${to.subtract(from)} from $from to $to in $frames frames"

  override def draw() {
    DrawMap.arrow(from, to, sim.realUnit.player.colorMedium)
  }
}
