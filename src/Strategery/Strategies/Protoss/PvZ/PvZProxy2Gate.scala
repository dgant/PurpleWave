package Strategery.Strategies.Protoss.PvZ

import Strategery.Maps.{MapGroups, StarCraftMap}
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import bwapi.Race

object PvZProxy2Gate extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvzOpenersTransitioningFrom2Gate)
  
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Zerg)
  
  override def prohibitedMaps: Iterable[StarCraftMap] = MapGroups.badForProxying
}
