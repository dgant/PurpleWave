package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.Pixel
import Utilities.?

final case class SimulationEventTween(sim: Simulacrum, to: Pixel, step: Pixel, frames: Int, reason: Option[String] = None) extends SimulationEvent(sim) {

  override def toString: String = f"$frame: ${sim.describe} targeting ${describe(target)} ${describePixel(targetAt)} tweens ${to.subtract(from)} (${to.subtract(from).length}px) from $from to $to ${?(to == step, "", f"by way of $step ")}over $frames frames${reason.map(": " + _).getOrElse("")}"

  override def draw(): Unit = {
    DrawMap.arrow(from, to, sim.realUnit.player.colorMedium)
  }
}
