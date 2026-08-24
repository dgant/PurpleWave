package Strategery.Strategies.AllRaces

import Gameplans.All.{ModalGameplan, StandardGameplan, WorkerRush => WorkerRushGameplan}
import Planning.Plans.Plan
import Strategery.Strategies.Strategy
import bwapi.Race

object WorkersKill    extends Strategy
object WorkersSpread  extends Strategy
object WorkersUnite   extends Strategy
object WorkersRaze    extends Strategy {
  setEnemyRace(Race.Terran, Race.Protoss)
}

abstract class WorkerRushStrategy extends Strategy {
  override def gameplan: Option[Plan] = Some(new ModalGameplan(new WorkerRushGameplan, new StandardGameplan))
  addChoice(WorkersSpread, WorkersUnite)
  addChoice(WorkersRaze, WorkersKill)
}

object WorkerRushImmediate extends WorkerRushStrategy
object WorkerRushOnScout extends WorkerRushStrategy {
  setStartLocationsMin(3)
}
object WorkerRushOnSupplyBlock extends WorkerRushStrategy
object WorkerRushContinuousProduction extends WorkerRushStrategy

object WorkerRushes {
  val all: Vector[Strategy] = Vector(WorkerRushImmediate, WorkerRushOnScout, WorkerRushOnSupplyBlock, WorkerRushContinuousProduction)
}
