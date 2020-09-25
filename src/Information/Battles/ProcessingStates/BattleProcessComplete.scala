package Information.Battles.ProcessingStates

class BattleProcessComplete extends BattleProcessState {
  override def step(): Unit = {
    transitionTo(new BattleProcessInitial)
  }

  override val isFinalStep: Boolean = true
}
