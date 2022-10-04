package Tactic.Tactics

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Utilities.?
import Utilities.UnitCounters.{CountOne, CountUpTo}
import Utilities.UnitFilters.IsWorker
import Utilities.UnitPreferences.PreferClose

class RemoveMineralBlocks extends Tactic {
  
  val miners = new LockUnits(this)
  miners.interruptable = false
  miners.matcher = IsWorker
  miners.counter = CountOne

  override def launch(): Unit = {
    val ourEdges = With.geography.ourZones.flatten(_.edges)
    val ourMineralBlocks = With.units.neutral.view
      .filter(unit => unit.unitClass.isMinerals && unit.isBlocker)
      .filter(unit => ourEdges.exists(edge => edge.contains(unit.pixel) || edge.pixelCenter.pixelDistanceSquared(unit.pixel) < Math.pow(32.0 * 3, 2)))
      .toVector
    if (ourMineralBlocks.isEmpty) return

    val workersTotal  = With.units.countOurs(IsWorker)
    val workersUsed   = With.geography.ourBases.map(b => 1 + 2 * b.minerals.length + 3 * b.gas.length).sum
    val workersFree   = workersTotal - workersUsed
    if (workersFree == 0 && workersTotal < ?(With.blackboard.wantToAttack(), 32, 39)) return

    val workersToUse = Math.max(1, Math.min(ourMineralBlocks.size, workersFree))
    miners.preference = PreferClose(ourMineralBlocks.head.pixel)
    miners.counter = CountUpTo(workersToUse)
    miners.acquire()
    miners.units.zipWithIndex.foreach(p => p._1.intend(this, new Intention { toGather = Some(ourMineralBlocks(p._2 % ourMineralBlocks.length)) }))
  }
}
