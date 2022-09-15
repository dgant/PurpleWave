package Tactic.Tactics

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountOne
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

    if (With.units.countOurs(IsWorker) < 39) return
    if (ourMineralBlocks.isEmpty) return
    
    val mineral = ourMineralBlocks.head
    miners.preference = PreferClose(mineral.pixel)
    miners.acquire()
    miners.units.foreach(_.intend(this, new Intention { toGather = Some(mineral) }))
  }
}
