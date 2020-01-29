package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvE.PvE3BaseIslandCarrier
import Strategery.Strategies.Strategy
import bwapi.Race

object CarriersWithNoDefense extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new PvE3BaseIslandCarrier) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def opponentsWhitelisted: Option[Iterable[String]] = Some(Iterable("Vajda", "Ximp"))
}
