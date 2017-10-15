package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.DarkArchonRushWithNoDefense
import Strategery.Strategies.Strategy
import bwapi.Race

object DarkArchonsWithNoDefense extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new DarkArchonRushWithNoDefense) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def restrictedOpponents: Option[Iterable[String]] = Some(Iterable("Vajda", "Ximp"))
}
