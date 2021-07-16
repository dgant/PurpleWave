package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.Pixel

final case class SimulationEventTween(sim: Simulacrum, to: Pixel, frames: Int, reason: Option[String] = None) extends SimulationEvent(sim) {

  override def toString: String = f"$frame: ${describe(sim)} targeting ${describe(target)} ${describePixel(targetAt)} tweens ${to.subtract(from)} from $from to $to over $frames frames${reason.map(": " + _).getOrElse("")}"

  override def draw() {
    DrawMap.arrow(from, to, sim.realUnit.player.colorMedium)
  }
}
