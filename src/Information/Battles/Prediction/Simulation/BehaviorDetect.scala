package Information.Battles.Prediction.Simulation

object BehaviorDetect extends SimulacrumBehavior {
  val fighting: Boolean = true
  override def act(simulacrum: Simulacrum): Unit = {
    // TODO: Update targets
    if (simulacrum.targets.isEmpty) {
      simulacrum.doBehavior(BehaviorFlee)
    }
  }
}
