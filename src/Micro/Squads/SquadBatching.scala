package Micro.Squads

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

trait SquadBatching {
  val batches = new mutable.Queue[SquadBatch]
  def startNewBatch(): Unit = {
    batches += new SquadBatch
  }

  protected var activeBatch: SquadBatch = new SquadBatch

  def noFreeBatches: Boolean = batches.headOption.forall(_.processingStarted)

  def freelance(freelancer: FriendlyUnitInfo) {
    if (noFreeBatches) startNewBatch()
    batches.last.freelancers += freelancer
  }

  def commission(squad: Squad): Unit = {
    if (noFreeBatches) startNewBatch()
    batches.last.squads += squad
  }

  def stepBatching(): Unit = {
    // Get the newest batch (Clearing all other batches that have accumulated in the interim)
    while (batches.size > 1 && batches.head.processingStarted == batches.head.processingFinished) {
      val nextBatch = batches.dequeue()
    }

    // While the batch isn't complete, update it
    if (batches.nonEmpty) {
      val batch = batches.head
      while ( ! batch.processingFinished && With.performance.continueRunning) {
        batch.step()
      }

      if (batch.processingFinished) {
        activeBatch = batches.dequeue()
        activeBatch.apply()
      }
    }
  }
}
