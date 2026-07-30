package Strategery.Strategies.Protoss

import Gameplans.Protoss.PvE.PvEProxy2Gate
import Lifecycle.With
import Planning.Plans.Plan
import Strategery.Strategies.Strategy
import bwapi.Race

object PvEProxy2Gate extends Strategy {
  setOurRace(Race.Protoss)
  addSelectionRequirement(() => With.placement.proxyTwoGateFit.isDefined)
  override def gameplan: Option[Plan] = Some(new PvEProxy2Gate)
}
