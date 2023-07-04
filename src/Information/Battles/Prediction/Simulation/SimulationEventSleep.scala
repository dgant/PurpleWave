package Information.Battles.Prediction.Simulation

import Mathematics.Points.Pixel

final case class SimulationEventSleep(sim: Simulacrum, frames: Int, reason: Option[String] = None) extends SimulationEvent(sim) {

  override def toString: String = f"$frame: ${sim.describe} $from targeting ${describe(target)} ${describePixel(targetAt)} sleeps for $frames frames${reason.map(": " + _).getOrElse("")}"

  override val to: Pixel = from
}
