package Information.Battles.Prediction.Simulation

import Debugging.ToString

trait SimulacrumBehavior {
  val fighting: Boolean
  def act(simulacrum: Simulacrum): Unit
  override val toString: String = ToString(this).replace("Behavior", "")
}
