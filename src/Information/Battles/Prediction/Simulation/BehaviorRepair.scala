package Information.Battles.Prediction.Simulation

object BehaviorRepair extends SimulacrumBehavior {
  val fighting: Boolean = true
  override def act(simulacrum: Simulacrum): Unit = {
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
