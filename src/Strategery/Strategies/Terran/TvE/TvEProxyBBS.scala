package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvEProxyBBS
import Strategery.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEProxyBBS extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new TvEProxyBBS) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)

  override def mapsBlacklisted: Vector[StarCraftMap] = MapGroups.badForProxying
}
