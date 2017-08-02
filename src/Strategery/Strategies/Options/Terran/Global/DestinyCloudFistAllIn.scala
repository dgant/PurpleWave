package Strategery.Strategies.Options.Terran.Global

import Planning.Plan
import Planning.Plans.Terran.TerranVsZerg
import Strategery.Strategies.Strategy
import bwapi.Race

object DestinyCloudFistAllIn
  extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new TerranVsZerg) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
