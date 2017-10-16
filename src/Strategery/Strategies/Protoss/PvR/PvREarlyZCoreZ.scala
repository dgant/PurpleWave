package Strategery.Strategies.Protoss.PvR

import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import bwapi.Race

object PvREarlyZCoreZ extends Strategy {
  
  override lazy val choices = Vector(
    ProtossChoices.pvtOpenersTransitioningFrom1Gate,
    ProtossChoices.pvpOpenersWithTransitions,
    ProtossChoices.pvzOpenersTransitioningFrom1Gate)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
