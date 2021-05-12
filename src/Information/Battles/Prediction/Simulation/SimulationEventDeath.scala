package Information.Battles.Prediction.Simulation

import Mathematics.Points.Pixel

final case class SimulationEventDeath(sim: Simulacrum) extends SimulationEvent(sim) {

  override def toString: String = f"$frame: ${describe(sim)} dies"

  override val to: Pixel = from
}
