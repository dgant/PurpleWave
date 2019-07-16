package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvE.{PvE15BaseIslandCarrier, PvE1BaseIslandCarrier, PvE2BaseIslandCarrier, PvE3BaseIslandCarrier}
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvEIslandCarrier extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def islandMaps: Boolean = true
  override def groundMaps: Boolean = false
}

object PvE1BaseIslandCarrier extends PvEIslandCarrier {
  override def gameplan: Option[Plan] = { Some(new PvE1BaseIslandCarrier) }
}
object PvE15BaseIslandCarrier extends PvEIslandCarrier {
  override def gameplan: Option[Plan] = { Some(new PvE15BaseIslandCarrier) }
}
object PvE2BaseIslandCarrier extends PvEIslandCarrier {
  override def gameplan: Option[Plan] = { Some(new PvE2BaseIslandCarrier) }
}
object PvE3BaseIslandCarrier extends PvEIslandCarrier {
  override def gameplan: Option[Plan] = { Some(new PvE3BaseIslandCarrier) }
}


