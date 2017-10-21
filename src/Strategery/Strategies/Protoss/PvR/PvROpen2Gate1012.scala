package Strategery.Strategies.Protoss.PvR

import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import bwapi.Race

object PvROpen2Gate1012 extends Strategy {
  
  override lazy val choices = Vector(
    ProtossChoices.pvtOpenersTransitioningFrom2Gate,
    ProtossChoices.pvpOpenersTransitioningFrom2Gate,
    ProtossChoices.pvzOpenersTransitioningFrom2Gate)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
