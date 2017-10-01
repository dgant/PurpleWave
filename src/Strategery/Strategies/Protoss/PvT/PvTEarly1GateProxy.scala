package Strategery.Strategies.Protoss.PvT

import Strategery.Maps.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object PvTEarly1GateProxy extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      PvT2BaseGateway,
      PvT2BaseGatewayForever))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
  
  override def prohibitedMaps: Iterable[StarCraftMap] = MapGroups.badForProxying
}
