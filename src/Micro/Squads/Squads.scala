package Micro.Squads

import Performance.Tasks.TimedTask
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Squads extends TimedTask {

  private case class SquadBatch(squads: mutable.ArrayBuffer[Squad] = ArrayBuffer.empty, freelancers: mutable.ArrayBuffer[FriendlyUnitInfo] = ArrayBuffer.empty)
  private var _batchActive: SquadBatch = SquadBatch()
  private var _batchNext: SquadBatch = SquadBatch()

  def all: Seq[Squad] = _batchActive.squads.view
  def freelancersMutable: mutable.Buffer[FriendlyUnitInfo] = _batchNext.freelancers

  def commission(squad: Squad): Unit = { if ( ! _batchNext.squads.contains(squad)) _batchNext.squads += squad }
  def freelance(freelancer: FriendlyUnitInfo) { _batchNext.freelancers += freelancer }

  override protected def onRun(budgetMs: Long): Unit = {
    _batchActive = _batchNext
    _batchNext = SquadBatch()
    all.foreach(_.recalculateRosters())
    all.foreach(_.run())
  }
}
