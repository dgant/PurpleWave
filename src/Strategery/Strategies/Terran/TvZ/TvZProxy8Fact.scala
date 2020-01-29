package Strategery.Strategies.Terran.TvZ

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvZ.TvZProxy8Fact
import Strategery.Strategies.Strategy
import Strategery.{MapGroups, StarCraftMap}
import bwapi.Race

object TvZProxy8Fact extends Strategy {
  
  override def gameplan: Option[Plan] = Some(new TvZProxy8Fact)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
}
