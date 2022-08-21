package Tactic.Tactics

import Lifecycle.With
import Performance.Cache
import Planning.ResourceLocks.LockUnits
import Tactic.Tactics.WorkerPulls.{PullForFighters, PullForWall, PullVsProxy, PullVsWorkerRush}
import Utilities.UnitFilters.IsWorker
import Utilities.UnitPreferences.PreferClose

class PullWorkers extends Tactic {
  val workers = new LockUnits(this)
  workers.matcher = IsWorker

  override def launch(): Unit = {
    var requestedWorkers = 0
    requestedWorkers += workersVsProxy()()
    requestedWorkers += workersVsWorkerRush()()
    requestedWorkers += workersForFighters()()
    requestedWorkers += workersForWall()()
    if (requestedWorkers == 0) return
    workers.preference = PreferClose(With.scouting.enemyThreatOrigin.center)
  }

  private val workersVsProxy      = new Cache(() => new PullVsProxy)
  private val workersVsWorkerRush = new Cache(() => new PullVsWorkerRush)
  private val workersForFighters  = new Cache(() => new PullForFighters)
  private val workersForWall      = new Cache(() => new PullForWall)
}
