package Tactic.Tactics

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.Predicates.MacroFacts
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

    if (With.units.countOurs(IsWorker) < ?(With.blackboard.wantToAttack(), 32, 39)) return
    if (ourMineralBlocks.isEmpty) return
    
    val mineral = ourMineralBlocks.head
    miners.preference = PreferClose(mineral.pixel)
    miners.counter = CountUpTo(Maff.clamp(ourMineralBlocks.size, 1, MacroFacts.unitsComplete(IsWorker) - With.geography.ourBases.map(b => 2 * b.minerals.size + 3 * b.gas.count(_.isOurs)).sum))
    miners.acquire()
    miners.units.foreach(_.intend(this, new Intention { toGather = Some(mineral) }))
  }
}
