package Strategery.Strategies.AllRaces

import Planning.Plan
import Planning.Plans.GamePlans.All.Sandbox
import Strategery.Strategies.Strategy

object Sandbox extends Strategy {
  override def gameplan: Option[Plan] = Some(new Sandbox)
}
