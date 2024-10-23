package Strategery.Strategies.AllRaces

import Gameplans.All.Sandbox
import Planning.Plan
import Strategery.Strategies.Strategy

object Sandbox extends Strategy {
  override def gameplan: Option[Plan] = Some(new Sandbox)
}
