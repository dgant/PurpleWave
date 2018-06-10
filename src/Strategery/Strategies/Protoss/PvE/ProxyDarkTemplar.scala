package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvE.ProxyDarkTemplarRush
import Strategery.Maps.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object ProxyDarkTemplar extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new ProxyDarkTemplarRush) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
  
  override def prohibitedMaps: Vector[StarCraftMap] = MapGroups.badForProxying
}
