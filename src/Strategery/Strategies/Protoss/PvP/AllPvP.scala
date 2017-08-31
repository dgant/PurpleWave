package Strategery.Strategies.Protoss.PvP

import Strategery.Strategies.Strategy
import bwapi.Race

object AllPvP extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvP1GateRoboObs,
      PvP2GateRoboObs,
      PvP2GateDT))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Protoss)
}
