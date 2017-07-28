package Planning.Plans.Macro.Expanding

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan

class RemoveMineralBlocks extends Plan {
  
  description.set("Remove nearby mineral blocks")
  
  val miners = new Property[LockUnits](new LockUnits)
  miners.get.unitMatcher.set(UnitMatchWorkers)
  miners.get.unitCounter.set(UnitCountOne)
  miners.get.interruptable.set(false)
  
  override def onUpdate() {
    val ourEdges = With.geography.ourZones.flatten(_.edges)
    val ourMineralBlocks = With.units.neutral
      .filter(unit => unit.unitClass.isMinerals && unit.mineralsLeft <= With.configuration.blockerMineralThreshold)
      .filter(unit => ourEdges.exists(edge => edge.contains(unit.pixelCenter) || edge.centerPixel.pixelDistanceSquared(unit.pixelCenter) < Math.pow(32.0 * 3, 2)))
    
    if (ourMineralBlocks.isEmpty) return
    
    val mineral = ourMineralBlocks.head
    miners.get.unitPreference.set(new UnitPreferClose(mineral.pixelCenter))
    miners.get.acquire(this)
    miners.get.units.foreach(_.agent.intend(this, new Intention {
      toGather = Some(mineral)
      canCower = true
    }))
  }
}
