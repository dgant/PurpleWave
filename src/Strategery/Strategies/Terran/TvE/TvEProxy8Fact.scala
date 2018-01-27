package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.Proxy8Fact
import Strategery.Maps.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEProxy8Fact extends Strategy {
  
  // Currently unusable due to delayed gas mining.
  
  override def gameplan: Option[Plan] = Some(new Proxy8Fact)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  
  override def prohibitedMaps: Iterable[StarCraftMap] = MapGroups.badForProxying
}
