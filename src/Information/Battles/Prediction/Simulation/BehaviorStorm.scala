package Information.Battles.Prediction.Simulation

object BehaviorStorm extends SimulacrumBehavior {
  override val fighting: Boolean = true
  override def act(simulacrum: Simulacrum): Unit = {
    // TODO
    simulacrum.doBehavior(BehaviorFlee)
  }
}
