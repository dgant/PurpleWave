package Information.Battles.Prediction.Simulation

object BehaviorHeal extends SimulacrumBehavior {
  val fighting: Boolean = true
  @inline override def act(simulacrum: Simulacrum): Unit = {
    // TODO
    simulacrum.doBehavior(BehaviorFlee)
  }
}
