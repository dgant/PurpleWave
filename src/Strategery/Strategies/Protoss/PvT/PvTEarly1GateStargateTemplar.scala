package Strategery.Strategies.Protoss.PvT

import Strategery.Strategies.Strategy
import bwapi.Race

object PvTEarly1GateStargateTemplar extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(PvT2BaseArbiter))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown, Race.Terran)
}
