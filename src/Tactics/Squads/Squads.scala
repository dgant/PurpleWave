package Tactics.Squads

import Lifecycle.With
import Performance.Tasks.TimedTask

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Squads extends TimedTask {

  private case class SquadBatch(var id: Int, squads: mutable.ArrayBuffer[Squad] = ArrayBuffer.empty)

  private var _batchActive: SquadBatch = SquadBatch(0)
  private var _batchNext: SquadBatch = SquadBatch(1)

  def all: Seq[Squad] = _batchActive.squads

  @inline final def isCommissioned(squad: Squad): Boolean = squad.batchId == _batchNext.id
  @inline final def commission(squad: Squad): Unit = {
    _batchNext.squads += squad
    squad.batchId = _batchNext.id
  }

  override protected def onRun(budgetMs: Long): Unit = {
    With.units.ours.foreach(_.setSquad(None))
    With.recruiter.locks.view.map(_.owner).filter(_.isInstanceOf[Squad]).map(_.asInstanceOf[Squad]).foreach(_.commission())
    all.foreach(_.clearUnits())
    all.foreach(squad => squad.addUnits(With.recruiter.lockedBy(squad)))
    val batchSwap = _batchActive
    _batchActive = _batchNext
    _batchNext = batchSwap
    _batchNext.id = _batchActive.id + 1
    _batchNext.squads.clear()
    all.foreach(_.prepareToRun())
    all.foreach(_.run())
  }
}
