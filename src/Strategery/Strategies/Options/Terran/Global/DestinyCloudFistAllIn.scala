package Strategery.Strategies.Options.Terran.Global

import Planning.Plan
import Planning.Plans.Terran.DestinyCloudFist
import Strategery.Strategies.Strategy
import bwapi.Race

object DestinyCloudFistAllIn
  extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new DestinyCloudFist) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
