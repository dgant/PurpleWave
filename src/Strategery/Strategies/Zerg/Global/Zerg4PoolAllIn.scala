package Strategery.Strategies.Zerg.Global

import Planning.Plan
import Planning.Plans.Zerg.GamePlans.Zerg4Pool
import Strategery.Strategies.Strategy
import bwapi.Race

object Zerg4PoolAllIn extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def gameplan: Option[Plan] = Some(new Zerg4Pool)
}