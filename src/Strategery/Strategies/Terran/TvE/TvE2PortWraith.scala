package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvE2PortWraith
import Strategery.Strategies.Strategy
import bwapi.Race

object TvE2PortWraith extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new TvE2PortWraith) }
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
}
