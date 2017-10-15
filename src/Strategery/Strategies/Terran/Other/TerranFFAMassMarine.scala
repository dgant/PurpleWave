package Strategery.Strategies.Terran.Other

import Planning.Plan
import Planning.Plans.Terran.GamePlans.FFAMassMarine
import Strategery.Strategies.Strategy
import bwapi.Race

object TerranFFAMassMarine  extends Strategy {
  
  override def gameplan(): Option[Plan] = { Some(new FFAMassMarine) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  
  override def ffa: Boolean = true
}
