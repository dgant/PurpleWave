package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvE.ThreeBaseCarriersWithNoDefense
import Strategery.Strategies.Strategy
import bwapi.Race

object CarriersWithNoDefense extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new ThreeBaseCarriersWithNoDefense) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def restrictedOpponents: Option[Iterable[String]] = Some(Iterable("Vajda", "Ximp", "PurpleWave"))
}
