package Information.Battles.Prediction.Simulation

object BehaviorAssemble extends SimulacrumBehavior {
  override val fighting: Boolean = false

  override def act(simulacrum: Simulacrum): Unit = {
    simulacrum.tween(simulacrum.simulation.enemyVanguard, Some("Assemble"))
  }
}
