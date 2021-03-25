package Information.Battles.Prediction.Simulation

object BehaviorHeal extends SimulacrumBehavior {
  @inline override def act(simulacrum: NewSimulacrum): Unit = {
    // TODO: Clear invalid targets
    // TODO: Sort targets
    simulacrum.targets.clear()
    if (simulacrum.targets.isEmpty) {
      simulacrum.behavior = BehaviorFlee
      simulacrum.act()
      return
    }
  }
}
