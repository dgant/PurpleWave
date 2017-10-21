package Strategery.Strategies.Protoss.PvR

import Strategery.Maps.{MapGroups, StarCraftMap}
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import bwapi.Race

object PvROpenProxy2Gate extends Strategy {
  
  override lazy val choices = Vector(
    ProtossChoices.pvtOpenersTransitioningFrom2Gate,
    ProtossChoices.pvpOpenersTransitioningFrom2Gate,
    ProtossChoices.pvzOpenersTransitioningFrom2Gate)
  
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Unknown)
  
  override def prohibitedMaps: Iterable[StarCraftMap] = MapGroups.badForProxying
}
