package Strategery.Strategies.Protoss.PvR

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Standard.PvR.PvRTinfoil
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import bwapi.Race

object PvRTinfoil extends Strategy {
  
  override lazy val choices = Vector(
    ProtossChoices.pvtOpenersTransitioningFrom2Gate,
    ProtossChoices.pvpOpenersWithTransitions,
    ProtossChoices.pvzOpenersTransitioningFrom2Gate)
  
  override def gameplan   : Option[Plan]    = Some(new PvRTinfoil)
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Unknown)
}
