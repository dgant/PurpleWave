package Strategery.Strategies.Options.Protoss.Global

import Strategery.Strategies.Options.Protoss.PvT._
import Strategery.Strategies.Strategy
import bwapi.Race

object AllPvT extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvTEarly14Nexus,
      PvTEarlyDTExpand,
      PvTEarly1GateRange,
      PvTEarly1015GateGoon),
    Vector(
      PvTLateArbiters,
      PvTLateCarriers,
      PvTLateMassGateway))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}
