package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.TwoBaseStove
import Strategery.Strategies.Strategy
import bwapi.Race

object Stove extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new TwoBaseStove) }
  
  override def ourRaces   : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race] = Vector(Race.Terran,Race.Zerg, Race.Unknown)
}
