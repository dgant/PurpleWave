package Planning.Plans.Macro

import Micro.Intent.Intention
import Planning.Composition.PositionFinders.Generic.TileSpecific
import Planning.Composition.Property
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchWorker
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import Planning.Composition.ResourceLocks.LockUnits
import Lifecycle.With

import Utilities.EnrichPosition._

class RemoveMineralBlocks extends Plan {
  
  description.set("Remove nearby mineral blocks")
  
  val miners = new Property[LockUnits](new LockUnits)
  miners.get.unitMatcher.set(UnitMatchWorker)
  miners.get.unitCounter.set(UnitCountOne)
  
  override def update() {
    val ourEdges = With.geography.ourZones.flatten(_.edges)
    val ourMineralBlocks = With.units.neutral
      .filter(unit => unit.unitClass.isMinerals && unit.mineralsLeft <= 24)
      .filter(unit => ourEdges.exists(edge => edge.contains(unit.pixelCenter) || edge.centerPixel.pixelDistanceSquared(unit.pixelCenter) < Math.pow(32.0 * 3, 2)))
    
    if (ourMineralBlocks.isEmpty) return
    
    val mineral = ourMineralBlocks.head
    miners.get.unitPreference.set(new UnitPreferClose { positionFinder.set(new TileSpecific(mineral.tileIncludingCenter)) } )
    miners.get.acquire(this)
    miners.get.units.foreach(unit => With.executor.intend(new Intention(this, unit) { toGather = Some(mineral) }))
  }
}
