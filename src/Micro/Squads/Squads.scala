package Micro.Squads

import Performance.Tasks.TimedTask
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Squads extends TimedTask {

  private case class SquadBatch(
    id: Int,
    squads: mutable.ArrayBuffer[Squad] = ArrayBuffer.empty,
    freelancers: mutable.ArrayBuffer[FriendlyUnitInfo] = ArrayBuffer.empty)

  private var _batchActive: SquadBatch = SquadBatch(0)
  private var _batchNext: SquadBatch = SquadBatch(1)

  def all: Seq[Squad] = _batchActive.squads.view
  def freelancersMutable: mutable.Buffer[FriendlyUnitInfo] = _batchNext.freelancers

  @inline final def isCommissioned(squad: Squad): Boolean = squad.batchId == _batchNext.id
  @inline final def commission(squad: Squad): Unit = {
    _batchNext.squads += squad
    squad.batchId = _batchNext.id
  }
  def freelance(freelancer: FriendlyUnitInfo) { _batchNext.freelancers += freelancer }

  override protected def onRun(budgetMs: Long): Unit = {
    _batchActive = _batchNext
    _batchNext = SquadBatch(_batchActive.id + 1)
    all.foreach(_.run())
  }
}
