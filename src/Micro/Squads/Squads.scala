package Micro.Squads

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class Squads {

  private var batchActive: SquadBatch = new SquadBatch
  private var batchNext: SquadBatch = new SquadBatch

  def all: Seq[Squad] = batchActive.squads.view

  def commission(squad: Squad): Unit = {
    batchNext.squads += squad
  }

  def freelance(freelancer: FriendlyUnitInfo) {
    batchNext.freelancers += freelancer
  }

  def finishRecruitment(): Unit = {
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

  def runSquads() {
    all.foreach(_.run())
  }
}
