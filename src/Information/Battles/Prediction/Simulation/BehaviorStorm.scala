package Information.Battles.Prediction.Simulation

object BehaviorStorm extends SimulacrumBehavior {
  override val fighting: Boolean = true
  override def act(simulacrum: NewSimulacrum): Unit = {
    // TODO
    simulacrum.doBehavior(BehaviorFlee)
  }
}
