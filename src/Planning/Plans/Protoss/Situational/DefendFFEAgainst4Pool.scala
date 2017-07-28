package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import ProxyBwapi.Races.{Protoss, Zerg}

class DefendFFEAgainst4Pool extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWorkers)
  
  override def onUpdate() {
  
    lazy val zerglings    = With.units.enemy.find(_.is(Zerg.Zergling))
    lazy val threatSource = zerglings.map(_.pixelCenter).getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
    lazy val cannons      = With.units.ours.filter(_.is(Protoss.PhotonCannon))
    
    if (cannons.isEmpty) return
    
    val toDefend        = cannons.minBy(_.pixelDistanceFast(threatSource)).pixelCenter.project(threatSource, 48.0)
    val workerCount     = With.units.ours.count(_.unitClass.isWorker)
    val workersCap      = workerCount - 3
    val workersDesired  = 12 - 3 * cannons.count(_.complete)
    val workersFinal    = Math.max(0, Math.min(workersCap, workersDesired))
    
    defenders.get.unitCounter.set(UnitCountExactly(workerCount - 4))
    defenders.get.acquire(this)
    defenders.get.units.foreach(_.intend(new Intention(this) {
      toTravel = Some(toDefend)
      canFlee = false
    }))
  }
}
