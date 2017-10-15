package Strategery.Strategies.Protoss.PvR

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.ProtossTinfoil
import Strategery.Strategies.Strategy
import bwapi.Race

object PvRTinfoil extends Strategy {
  
  override def gameplan: Option[Plan] = Some(new ProtossTinfoil)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
