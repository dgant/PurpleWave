package Information.Battles.ProcessingStates

class BattleProcessInitial extends BattleProcessState {
  override def step(): Unit = {
    transitionTo(new BattleProcessCluster)
  }
}
