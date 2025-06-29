package Tactic.Squads

import Lifecycle.With
import Performance.Cache
import Performance.Tasks.TimedTask
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Try

class Squads extends TimedTask {

  var frameRun          : Int = 0
  var frameRunPrevious  : Int = 0

  private case class SquadBatch(var id: Int, squads: mutable.ArrayBuffer[Squad] = ArrayBuffer.empty)

  private var _batchActive  : SquadBatch = SquadBatch(0)
  private var _batchNext    : SquadBatch = SquadBatch(1)

  def all   : Seq[Squad] = _batchActive.squads
  def next  : Seq[Squad] = _batchNext.squads

  def enemies: Map[Squad, Set[UnitInfo]] = _enemies()
  private val _enemies = new Cache(() => all.filter(_.enemies.nonEmpty).map(s => (s, s.enemies.toSet)).toMap)

  @inline final def isCommissioned(squad: Squad): Boolean = squad.batchId == _batchNext.id
  @inline final def commission(squad: Squad): Unit = {
    _batchNext.squads += squad
    squad.batchId = _batchNext.id
  }

  override protected def onRun(budgetMs: Long): Unit = {
    frameRunPrevious = frameRun
    frameRun = With.frame
    With.units.ours.foreach(_.setSquad(None))
    With.recruiter.locks.view
      .flatMap(lock => Try(lock.owner.asInstanceOf[Squad]).toOption)
      .foreach(_.commission())
    all.foreach(_.clearUnits())
    all.foreach(squad => squad.addUnits(With.recruiter.lockedBy(squad)))
    val batchSwap = _batchActive
    _batchActive  = _batchNext
    _batchNext    = batchSwap
    _batchNext.id = _batchActive.id + 1
    _batchNext.squads.clear()
    all.foreach(_.prepareToRun())
    all.foreach(_.run())
  }
}
