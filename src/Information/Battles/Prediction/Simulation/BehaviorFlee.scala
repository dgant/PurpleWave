package Information.Battles.Prediction.Simulation

object BehaviorFlee extends SimulacrumBehavior {
  val fighting: Boolean = false
  @inline override def act(simulacrum: NewSimulacrum): Unit = {
    if (simulacrum.unitClass.topSpeed > 0) { simulacrum.doBehavior(BehaviorIdle) }
  }
}
