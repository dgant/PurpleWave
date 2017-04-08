package Information.Battles.Simulation.Tactics

object TacticWorkers extends Enumeration {
  type BattleStrategyWorkers = Value
  val Ignore, Flee, HalfFight, AllFight = Value
}
