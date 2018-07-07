package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvE.DarkArchonRushWithNoDefense
import Strategery.Strategies.Strategy
import bwapi.Race

object DarkArchonsWithNoDefense extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new DarkArchonRushWithNoDefense) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def opponentsWhitelisted: Option[Iterable[String]] = Some(Iterable("Vajda", "Ximp"))
}
