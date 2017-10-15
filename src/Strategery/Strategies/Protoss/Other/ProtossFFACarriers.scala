package Strategery.Strategies.Protoss.Other

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.ThreeBaseCarriersWithNoDefense
import Planning.Plans.Protoss._
import Strategery.Strategies.Strategy
import bwapi.Race

object ProtossFFACarriers extends Strategy {
  
  override def gameplan: Option[Plan] = {  Some(new ThreeBaseCarriersWithNoDefense) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def ffa = true
}
