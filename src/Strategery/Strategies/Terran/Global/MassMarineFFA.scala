package Strategery.Strategies.Terran.Global

import Planning.Plan
import Planning.Plans.Terran.GamePlans.FFAMassMarine
import Strategery.Strategies.Strategy
import bwapi.Race

object MassMarineFFA  extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new FFAMassMarine) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  
  override def ffa: Boolean = true
}
