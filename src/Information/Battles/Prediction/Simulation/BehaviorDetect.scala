package Information.Battles.Prediction.Simulation

object BehaviorDetect extends SimulacrumBehavior {
  override def act(simulacrum: NewSimulacrum): Unit = {
    // TODO: Update targets
    if (simulacrum.targets.isEmpty) {
      simulacrum.doBehavior(BehaviorFlee)
    }
  }
}
