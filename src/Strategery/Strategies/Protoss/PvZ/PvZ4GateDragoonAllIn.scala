package Strategery.Strategies.Protoss.PvZ

import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import bwapi.Race

object PvZ4GateDragoonAllIn extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvzMidgameTransitioningFromOneBase)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Zerg)
}
