package Strategery.Strategies.Protoss.PvT

import Strategery.Strategies.Strategy
import bwapi.Race

object PvTEarly1015GateGoonDT extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvT2BaseArbiter,
      PvT3BaseArbiter))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}
