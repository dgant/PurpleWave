package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Micro.Intent.Intention
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
    lazy val cannons      = With.units.ours.filter(_.is(Protoss.PhotonCannon)).map(_.pixelCenter.project(threatSource, 48.0))
    
    if (cannons.isEmpty) return
    
    val toDefend = cannons.minBy(_.pixelDistanceFast(threatSource))
    val workerCount = With.units.ours.count(_.unitClass.isWorker)

    defenders.get.unitCounter.set(UnitCountExactly(workerCount - 4))
    defenders.get.acquire(this)
    defenders.get.units.foreach(defender => With.executor.intend(new Intention(this, defender) {
      toTravel = Some(toDefend)
      canFlee = false
    }))
  }
}
