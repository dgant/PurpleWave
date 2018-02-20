package Information.Battles.Prediction.Simulation

trait SimulationEvent {
  protected def describe(sim: Simulacrum): String = {
    sim.realUnit.unitClass.toString + " #" + sim.realUnit.id
  }
  
  def frame: Int
  
  def draw() {}
}
