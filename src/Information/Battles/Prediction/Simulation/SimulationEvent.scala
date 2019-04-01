package Information.Battles.Prediction.Simulation

import Mathematics.Points.Pixel

trait SimulationEvent {
  protected def describe(sim: Simulacrum): String = {
    (if (sim.realUnit.isFriendly) "F" else "E") + " " + sim.realUnit.unitClass.toString + " #" + sim.realUnit.id
  }
  
  def frame: Int
  
  def draw() {}
  def from: Pixel
  def to: Pixel
}
