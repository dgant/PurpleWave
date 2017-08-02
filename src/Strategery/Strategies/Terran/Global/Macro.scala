package Strategery.Strategies.Terran.Global

import Planning.Plan
import Planning.Plans.Terran.GamePlans.TerranGamePlan
import Strategery.Strategies.Strategy
import bwapi.Race

object Macro
  extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new TerranGamePlan) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
