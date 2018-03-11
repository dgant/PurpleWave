package Strategery.Strategies.Protoss.PvT

import Strategery.Strategies.Strategy
import bwapi.Race

object PvTEarly1GateStargate extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvT2BaseArbiter,
      PvT2BaseCarrier))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}
