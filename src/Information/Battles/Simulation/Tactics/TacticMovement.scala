package Information.Battles.Simulation.Tactics

object TacticMovement extends Enumeration {
  type BattleStrategyMovement = Value
  val Ignore, Charge, Kite, Flee = Value
}
