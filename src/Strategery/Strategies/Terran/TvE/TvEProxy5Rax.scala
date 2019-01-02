package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvEProxy5Rax
import Strategery.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEProxy5Rax extends Strategy {
  
  override def gameplan: Option[Plan] = Some(new TvEProxy5Rax)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  
  override def mapsBlacklisted: Vector[StarCraftMap] = MapGroups.badForProxying
}
