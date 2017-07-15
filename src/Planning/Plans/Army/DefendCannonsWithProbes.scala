package Planning.Plans.Army

import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import ProxyBwapi.Races.Protoss

class DefendCannonsWithProbes extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWorkers)
  
  override def onUpdate() {
    val threatSource = With.intelligence.mostBaselikeEnemyTile.pixelCenter
    val cannons = With.units.ours.filter(_.is(Protoss.PhotonCannon))
    val cannonToDefend = cannons
      .toVector
      .sortBy(_.pixelDistanceFast(threatSource))
      .headOption
    
    if (cannonToDefend.isEmpty) {
      return
    }
  
    val destination = cannonToDefend.get.pixelCenter.project(threatSource, 48.0)
    val workerCount = With.units.ours.count(_.unitClass.isWorker)
    
    defenders.get.unitCounter.set(UnitCountExactly(workerCount - 4))
    defenders.get.acquire(this)
    defenders.get.units.foreach(defender => With.executor.intend(new Intention(this, defender) {
      toTravel = Some(destination)
    }))
  }
}
