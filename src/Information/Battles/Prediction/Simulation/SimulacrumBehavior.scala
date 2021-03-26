package Information.Battles.Prediction.Simulation

trait SimulacrumBehavior {
  val fighting: Boolean
  def act(simulacrum: NewSimulacrum): Unit
}
