package Tactic.Tactics

import Lifecycle.With
import Planning.ResourceLocks.LockUnits
import Utilities.?
import Utilities.UnitCounters.{CountOne, CountUpTo}
import Utilities.UnitFilters.IsWorker
import Utilities.UnitPreferences.PreferClose

class TacticRemoveMineralBlocks extends Tactic {
  
  val miners: LockUnits = new LockUnits(this, IsWorker, CountOne, interruptable = false)

  private val blockerRadiusSquared = Math.pow(32 * 5, 2)
  override def launch(): Unit = {
    val ourEdges = With.geography.ourMetros.flatten(_.edges)
    val ourMineralBlocks = With.units.neutral.view
      .filter(unit => unit.unitClass.isMinerals && unit.isBlocker)
      .filter(unit => ourEdges.exists(edge => edge.contains(unit.pixel) || edge.pixelCenter.pixelDistanceSquared(unit.pixel) < blockerRadiusSquared))
      .toVector
    if (ourMineralBlocks.isEmpty) return

    val workersTotal  = With.units.countOurs(IsWorker)
    val workersUsed   = With.geography.ourBases.map(b => 1 + 2 * b.minerals.length + 3 * b.gas.length).sum
    val workersFree   = workersTotal - workersUsed
    if (workersFree <= 0 && workersTotal < ?(With.blackboard.wantToAttack(), 32, 39)) return

    val workersToUse = Math.max(1, Math.min(ourMineralBlocks.size, workersFree))
    miners
      .setPreference(PreferClose(ourMineralBlocks.head.pixel))
      .setCounter(CountUpTo(workersToUse))
      .acquire()
      .zipWithIndex
      .foreach(p => p._1.intend(this).setGather(ourMineralBlocks(p._2 % ourMineralBlocks.length)))
  }
}
