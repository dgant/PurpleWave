package Strategery.Strategies.Protoss.PvT

import Strategery.Strategies.Strategy
import bwapi.Race

object PvTEarly1GateRange extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvT2BaseGateway,
      PvT2BaseCarrier,
      PvT3BaseCorsair))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown, Race.Terran)
}
