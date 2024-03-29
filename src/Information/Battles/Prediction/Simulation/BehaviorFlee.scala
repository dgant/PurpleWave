package Information.Battles.Prediction.Simulation

object BehaviorFlee extends SimulacrumBehavior {
  val fighting: Boolean = false
  @inline override def act(simulacrum: Simulacrum): Unit = {
    if ( ! simulacrum.canMove) {
      simulacrum.doBehavior(BehaviorIdle)
      return
    }

    // TODO: Flee most recent attacker
  }
}
