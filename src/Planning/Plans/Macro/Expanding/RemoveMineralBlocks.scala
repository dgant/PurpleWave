package Planning.Plans.Macro.Expanding

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.MatchWorkers
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Property}

class RemoveMineralBlocks extends Plan {
  
  description.set("Remove nearby mineral blocks")
  
  val miners = new Property[LockUnits](new LockUnits)
  miners.get.unitMatcher.set(MatchWorkers)
  miners.get.unitCounter.set(UnitCountOne)
  miners.get.interruptable.set(false)
  
  override def onUpdate() {
    val ourEdges = With.geography.ourZones.flatten(_.edges)
    val ourMineralBlocks = With.units.neutral.view
      .filter(unit => unit.unitClass.isMinerals && unit.mineralsLeft <= With.configuration.blockerMineralThreshold)
      .filter(unit => ourEdges.exists(edge => edge.contains(unit.pixel) || edge.pixelCenter.pixelDistanceSquared(unit.pixel) < Math.pow(32.0 * 3, 2)))
      .toVector
    
    if (ourMineralBlocks.isEmpty) return
    
    val mineral = ourMineralBlocks.head
    miners.get.unitPreference.set(UnitPreferClose(mineral.pixel))
    miners.get.acquire(this)
    miners.get.units.foreach(_.agent.intend(this, new Intention {
      toGather = Some(mineral)
    }))
  }
}
