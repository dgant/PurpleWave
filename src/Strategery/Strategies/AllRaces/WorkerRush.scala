package Strategery.Strategies.AllRaces

import Planning.Plan
import Planning.Plans.GamePlans.AllRaces.WorkerRush
import Strategery.Strategies.Strategy
import bwapi.Race

object WorkersSpread extends Strategy
object WorkersUnite extends Strategy
object WorkersRaze extends Strategy {
  override def enemyRaces: Iterable[Race] = Iterable(Race.Terran, Race.Protoss)
}
object WorkersKill extends Strategy

abstract class WorkerRushStrategy extends Strategy {
  override def gameplan: Option[Plan] = Some(new WorkerRush)

  override def choices: Iterable[Iterable[Strategy]] = Iterable(
    Iterable(WorkersSpread, WorkersUnite),
    Iterable(WorkersRaze, WorkersKill)
  )
}

object WorkerRushImmediate            extends WorkerRushStrategy
object WorkerRushOnScout              extends WorkerRushStrategy {
  override def startLocationsMin: Int = 3
}
object WorkerRushOnSupplyBlock        extends WorkerRushStrategy
object WorkerRushContinuousProduction extends WorkerRushStrategy

object WorkerRushes {
  val all = Vector(WorkerRushImmediate, WorkerRushOnScout, WorkerRushOnSupplyBlock, WorkerRushContinuousProduction)
}
