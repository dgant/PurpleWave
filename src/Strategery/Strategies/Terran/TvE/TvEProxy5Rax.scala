package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.Proxy5Rax
import Strategery.Maps.MapGroups
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEProxy5Rax extends Strategy {
  
  override def gameplan: Option[Plan] = Some(new Proxy5Rax)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  
  override def prohibitedMaps = MapGroups.badForProxying
}
