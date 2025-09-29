package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.Pixel

final case class SimulationEventMove(sim: Simulacrum, to: Pixel, reason: Option[String] = None) extends SimulationEvent(sim) {

  override def toString: String = f"$frame: ${sim.describe} targeting ${describe(target)} ${describePixel(targetAt)} moves from ${sim.pixel} towards $to ${reason.map(": " + _).getOrElse("")}"

  override def draw(): Unit = {
    DrawMap.arrow(from, to, sim.realUnit.player.colorMedium)
  }
}
