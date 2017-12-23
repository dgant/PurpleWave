package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.TvE.Proxy5Rax
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEProxy5RaxAllIn extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new Proxy5Rax) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  
  override def startLocationsMax = 2
}
