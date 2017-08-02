package Strategery.Strategies.Options.Terran.Global

import Planning.Plan
import Planning.Plans.Terran.GamePlans.MassMarine
import Strategery.Strategies.Strategy
import bwapi.Race

object MassMarineAllIn
  extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new MassMarine) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
