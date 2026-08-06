package Strategery.Strategies.Protoss

import Gameplans.Protoss.PvE.{PvECannonRushTestGameplan, PvEProxy2Gate}
import Lifecycle.With
import Placement.Access.PlaceLabels.Proxy2Gate
import Planning.Plans.Plan
import Strategery.Strategies.Strategy
import bwapi.Race

object PvEProxy2Gate extends Strategy {
  setOurRace(Race.Protoss)
  addSelectionRequirement(() => With.placement.get(Proxy2Gate).nonEmpty)
  override def gameplan: Option[Plan] = Some(new PvEProxy2Gate)
}

object PvECannonRushTest extends Strategy {
  setOurRace(Race.Protoss)
  override def gameplan: Option[Plan] = Some(new PvECannonRushTestGameplan)
}

