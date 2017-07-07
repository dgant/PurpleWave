package Strategery.Strategies.Options.Protoss.Global

import Strategery.Strategies.Options.Protoss.PvT._
import Strategery.Strategies.Strategy
import bwapi.Race

object AllPvT extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      Early14Nexus,
      EarlyDTExpand,
      Early1GateRange,
      Early1015GateGoon),
    Vector(
      LateArbiters,
      LateCarriers,
      LateMassGateway))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Terran)
}
