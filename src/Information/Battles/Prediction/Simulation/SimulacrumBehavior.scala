package Information.Battles.Prediction.Simulation

import Debugging.ToString

trait SimulacrumBehavior {
  val fighting: Boolean
  def act(simulacrum: NewSimulacrum): Unit
  override val toString: String = ToString(this).replace("Behavior", "")
}
