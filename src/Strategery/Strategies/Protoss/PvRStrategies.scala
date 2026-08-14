package Strategery.Strategies.Protoss

import Gameplans.Protoss.PvR.{PvR2Gate4Gate, PvRDT}
import Lifecycle.With
import Placement.Access.PlaceLabels
import Planning.Plans.Plan
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvRStrategy extends Strategy {
  override def ourRaces    : Seq[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Seq[Race] = Vector(Race.Unknown)
}

object PvR2Gate4Gate extends PvRStrategy {
  override def gameplan: Option[Plan] = Some(new PvR2Gate4Gate)
}

object PvRDT extends PvRStrategy {
  override def gameplan: Option[Plan] = Some(new PvRDT)
}

object PvR2GateProxy        extends PvRStrategy {
  addSelectionRequirement(() => With.placement.get(PlaceLabels.Proxy2Gate).nonEmpty)
}
object PvRCannonRushFE      extends PvRStrategy
object PvRCannonRushMaximum extends PvRStrategy
object PvR2Gate1012         extends PvRStrategy
object PvRZZCoreZ           extends PvRStrategy
object PvRFFE               extends PvRStrategy
object PvRGFE               extends PvRStrategy


