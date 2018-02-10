package Strategery.Strategies.Zerg.ZvP

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvP.HydraBust
import Strategery.Strategies.Strategy
import bwapi.Race

object HydraBust extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
  
  override def gameplan: Option[Plan] = { Some(new HydraBust) }
}
