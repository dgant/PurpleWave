package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.ProxyDarkTemplarRush
import Strategery.Strategies.Strategy
import bwapi.Race

object ProxyDarkTemplar extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new ProxyDarkTemplarRush) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
}
