package Planning.Plans.Macro.Expanding

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.MatchWorker
import Planning.UnitPreferences.PreferClose

class RemoveMineralBlocks extends Plan {
  
  val miners = new LockUnits(this)
  miners.interruptable = false
  miners.matcher = MatchWorker
  miners.counter = CountOne
  
  override def onUpdate() {
    val ourEdges = With.geography.ourZones.flatten(_.edges)
    val ourMineralBlocks = With.units.neutral.view
      .filter(unit => unit.unitClass.isMinerals && unit.mineralsLeft <= With.configuration.blockerMineralThreshold)
      .filter(unit => ourEdges.exists(edge => edge.contains(unit.pixel) || edge.pixelCenter.pixelDistanceSquared(unit.pixel) < Math.pow(32.0 * 3, 2)))
      .toVector
    
    if (ourMineralBlocks.isEmpty) return
    
    val mineral = ourMineralBlocks.head
    miners.preference = PreferClose(mineral.pixel)
    miners.acquire(this)
    miners.units.foreach(_.agent.intend(this, new Intention { toGather = Some(mineral) }))
  }
}
