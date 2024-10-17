package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.Gameplans.Terran.TvE.TvEProxyBBS
import Strategery.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEProxyBBS extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new TvEProxyBBS) }
  
  override def ourRaces: Seq[Race] = Seq(Race.Terran)

  override def mapsBlacklisted: Seq[StarCraftMap] = MapGroups.badForProxying
}
