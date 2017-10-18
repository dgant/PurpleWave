package Strategery.Strategies.Protoss.PvT

import Strategery.Maps.{MapGroups, StarCraftMap}
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import bwapi.Race

object PvTProxy2Gate extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvtOpenersTransitioningFrom2Gate)
  
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Terran)
  
  override def prohibitedMaps: Iterable[StarCraftMap] = MapGroups.badForProxying
}
