package Micro.Squads

import Performance.Tasks.TimedTask
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class Squads extends TimedTask {

  private var batchActive: SquadBatch = new SquadBatch
  private var batchNext: SquadBatch = new SquadBatch

  def all: Seq[Squad] = batchActive.squads.view

  def commission(squad: Squad): Unit = {
    batchNext.squads += squad
  }

  def freelance(freelancer: FriendlyUnitInfo) {
    batchNext.freelancers += freelancer
  }

  def recruit(): Unit = {
    while ( ! batchNext.processingFinished) {
      batchNext.step()
      if (batchNext.processingFinished) {
        batchActive = batchNext
        batchActive.finish()
        batchNext = new SquadBatch
        return
      }
    }
  }

  override protected def onRun(budgetMs: Long): Unit = {
    all.foreach(_.run())
  }
}
