package Information.Battles.Prediction.Simulation

object BehaviorDetect extends SimulacrumBehavior {
  val fighting: Boolean = true
  override def act(simulacrum: Simulacrum): Unit = {
    simulacrum.targets.removeIf(t => ! t.cloaked && ! t.burrowed)
    if (simulacrum.targets.isEmpty) {
      simulacrum.doBehavior(BehaviorFlee)
    }
  }
}
