package Planning.Plans.Macro

import Micro.Intentions.Intention
import Planning.Composition.PositionFinders.Generic.PositionSpecific
import Planning.Composition.Property
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchWorker
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import Planning.Plans.Allocation.LockUnits
import Startup.With

class RemoveMineralBlocks extends Plan {
  
  description.set("Remove nearby mineral blocks")
  
  val miners = new Property[LockUnits](new LockUnits)
  miners.get.unitMatcher.set(UnitMatchWorker)
  miners.get.unitCounter.set(UnitCountOne)
  
  override def onFrame() {
    val ourEdges = With.geography.ourZones.flatten(_.edges)
    val ourMineralBlocks = With.units.neutral
      .filter(unit => unit.unitClass.isMinerals && unit.mineralsLeft <= 24)
      .filter(unit => ourEdges.exists(edge => edge.contains(unit.pixelCenter) || edge.centerPixel.getDistance(unit.pixelCenter) < 32.0 * 3))
    
    if (ourMineralBlocks.isEmpty) return
    
    val mineral = ourMineralBlocks.head
    miners.get.unitPreference.set(new UnitPreferClose { positionFinder.set(new PositionSpecific(mineral.tileCenter)) } )
    miners.get.onFrame()
    miners.get.units.foreach(unit => With.executor.intend(new Intention(this, unit) { toGather = Some(mineral) }))
  }
}
