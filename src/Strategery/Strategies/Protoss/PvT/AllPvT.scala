package Strategery.Strategies.Protoss.PvT

import Strategery.Strategies.Strategy
import bwapi.Race

object AllPvT extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvTEarly14Nexus,
      PvTEarlyDTExpand,
      PvTEarly1GateRange,
      PvTEarly1GateReaver,
      PvTEarly2GateObs,
      PvTEarly1015GateGoon),
    Vector(
      PvTLateArbiters,
      PvTLateCarriers,
      PvTLateMassGateway))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}
