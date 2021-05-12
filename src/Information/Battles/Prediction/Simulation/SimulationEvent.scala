package Information.Battles.Prediction.Simulation

import Mathematics.Points.Pixel

abstract class SimulationEvent(sim: Simulacrum) {
  protected def describe(sim: Simulacrum): String = {
    f"${(if (sim.isFriendly) "F" else "E")} ${sim.unitClass.toString} #${sim.realUnit.id}"
  }

  val frame: Int = sim.simulation.prediction.simulationFrames
  val from: Pixel = sim.pixel

  def to: Pixel
  def draw() {}
}
