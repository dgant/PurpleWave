package Strategery.Strategies.Options.Zerg.Global

import Planning.Plan
import Planning.Plans.Zerg.GamePlans.Zerg9Hatch9PoolProxy
import Strategery.Strategies.Strategy
import bwapi.Race

object Zerg9Hatch9PoolProxyAllIn extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def buildGameplan(): Option[Plan] = Some(new Zerg9Hatch9PoolProxy)
}
