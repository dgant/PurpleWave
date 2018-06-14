package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import Micro.Agency.{Intention, Leash}
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountBetween
import Planning.UnitMatchers.UnitMatchWorkers
import Planning.{Plan, Property}
import ProxyBwapi.Races.{Protoss, Zerg}

abstract class DefendFFEWithProbes extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWorkers)
  
  protected def probeCount: Int
  
  override def onUpdate() {
  
    var cannons = With.units.ours.filter(_.is(Protoss.PhotonCannon))
    if (cannons.isEmpty) cannons = With.units.ours.filter(_.is(Protoss.Forge))
    
    lazy val zerglings    = With.units.enemy.find(_.is(Zerg.Zergling))
    lazy val threatSource = zerglings.map(_.pixelCenter).getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
    lazy val toDefend     = cannons.minBy(_.pixelDistanceCenter(threatSource)).pixelCenter.project(threatSource, 64.0)
    
    if (cannons.isEmpty) return
    
    val probesRequired = probeCount
    if (defenders.get.units.size > probesRequired) {
      defenders.get.release()
    }
    
    defenders.get.unitCounter.set(new UnitCountBetween(0, probesRequired))
    defenders.get.acquire(this)
    defenders.get.units.foreach(_.agent.intend(this, new Intention {
      canFlee   = false
      toForm    = Some(toDefend)
      toTravel  = Some(toDefend)
      toLeash   = Some(Leash(toDefend, 32.0 * 5.0))
    }))
  }
}
