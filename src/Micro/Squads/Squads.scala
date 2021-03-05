package Micro.Squads

import Lifecycle.With
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
    With.units.ours.foreach(_.setSquad(None))
    With.units.enemy.foreach(_.clearSquads())
    all.foreach(s => s.units.foreach(_.setSquad(Some(s))))
    all.foreach(s => s.enemies.foreach(_.foreign.foreach(_.addSquad(s))))
    all.foreach(_.run())
  }
}
