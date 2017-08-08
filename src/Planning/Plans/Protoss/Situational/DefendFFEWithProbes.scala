package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import ProxyBwapi.Races.{Protoss, Zerg}

abstract class DefendFFEWithProbes extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWorkers)
  
  protected def probeCount: Int
  
  override def onUpdate() {
  
    var cannons = With.units.ours.filter(_.is(Protoss.PhotonCannon))
    if (cannons.isEmpty) cannons = With.units.ours.filter(_.is(Protoss.Forge))
    
    lazy val  zerglings    = With.units.enemy.find(_.is(Zerg.Zergling))
    lazy val  threatSource = zerglings.map(_.pixelCenter).getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
    lazy val  toDefend     = cannons.minBy(_.pixelDistanceFast(threatSource)).pixelCenter.project(threatSource, 64.0)
    
    if (cannons.isEmpty) return
    
    defenders.get.unitCounter.set(UnitCountExactly(probeCount))
    defenders.get.acquire(this)
    defenders.get.units.foreach(_.agent.intend(this, new Intention {
      toTravel = Some(toDefend)
    }))
  }
}
