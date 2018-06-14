package Planning.Plans.GamePlans.Protoss.Situational

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountExactly
import Planning.UnitMatchers.UnitMatchWorkers
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Property}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

class DefendZealotsAgainst4Pool extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWorkers)
  
  override def onUpdate() {
    
    if (With.units.ours.exists(u => u.aliveAndComplete && u.is(Protoss.PhotonCannon))) return
    
    def inOurBase(unit: UnitInfo): Boolean = unit.zone.bases.exists(_.owner.isUs)
    
    val cannons           = With.units.ours .filter(u => u.aliveAndComplete && u.is(Protoss.PhotonCannon))
    val zealots           = With.units.ours .filter(u => u.aliveAndComplete && u.is(Protoss.Zealot) && inOurBase(u))
    lazy val zerglings    = With.units.enemy.filter(u => u.aliveAndComplete && u.is(Zerg.Zergling)  && inOurBase(u))
    lazy val workers      = With.units.ours.filter(u => u.aliveAndComplete && u.unitClass.isWorker)
    lazy val threatening  = zerglings.filter(_.inPixelRadius(32 * 4).exists(n => n.isOurs && n.totalHealth < 200))
    
    if (zealots.isEmpty) {
      return
    }
    if (zealots.size > 4) {
      return
    }
    if (cannons.nonEmpty) {
      return
    }
    if (zerglings.isEmpty) {
      return
    }
    if (threatening.isEmpty) {
      return
    }
    
    val workersNeeded   = 1 + 3 * zerglings.size - 3 * zealots.size
    val workerCap       = workers.size - 4
    val workersToFight  = Math.max(0, Math.min(workerCap, workersNeeded))
    val target          = zealots.minBy(zealot => zerglings.map(_.pixelDistanceEdge(zealot)).min).pixelCenter
    
    defenders.get.unitCounter.set(UnitCountExactly(workersToFight))
    defenders.get.unitPreference.set(UnitPreferClose(target))
    defenders.get.acquire(this)
    defenders.get.units.foreach(_.agent.intend(this, new Intention {
      toTravel = Some(target)
      canBerzerk = true
    }))
  }
}
