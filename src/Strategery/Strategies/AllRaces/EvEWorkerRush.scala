package Strategery.Strategies.AllRaces

import Gameplans.All.{ModalGameplan, StandardGameplan, WorkerRush => WorkerRushGameplan}
import Planning.Plans.Plan
import Strategery.Strategies.Strategy

object EvEWorkerRush extends Strategy {
  override def gameplan: Option[Plan] = Some(new WorkerRushGameplan)
}

object WorkerRushes {
  val all = Vector(EvEWorkerRush)
}