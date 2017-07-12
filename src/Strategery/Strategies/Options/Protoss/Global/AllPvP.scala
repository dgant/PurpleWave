package Strategery.Strategies.Options.Protoss.Global

import Strategery.Strategies.Options.Protoss.PvP._
import Strategery.Strategies.Strategy
import bwapi.Race

object AllPvP extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvPEarly1GateCore,
      PvPEarly1GateZZCore,
      PvPEarly2Gate910,
      PvPEarly2Gate1012,
      PvPEarlyFE,
      PvPEarlyFFE))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Protoss)
}
