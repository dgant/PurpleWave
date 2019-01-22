package Strategery.Strategies.Zerg

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.{Zerg4Pool, ZvESparkle}
import Strategery.Strategies.Strategy
import bwapi.Race

class ZergStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
}

object ZvE4Pool extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new Zerg4Pool)
}

object ZvESparkle extends ZergStrategy {
  override def gameplan: Option[Plan] = Some(new ZvESparkle)
  override def islandMaps: Boolean = true
  override def groundMaps: Boolean = false
}