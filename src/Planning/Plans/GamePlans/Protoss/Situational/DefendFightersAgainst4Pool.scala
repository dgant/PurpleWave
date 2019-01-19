package Planning.Plans.GamePlans.Protoss.Situational

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountExactly
import Planning.UnitMatchers.UnitMatchWorkers
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Property}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

class DefendFightersAgainst4Pool extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWorkers)
  
  override def onUpdate() {
    
    if (With.units.ours.exists(u => u.aliveAndComplete && u.is(Protoss.PhotonCannon))) return
    
    def inOurBase(unit: UnitInfo): Boolean = unit.zone.bases.exists(_.owner.isUs)
    
    val cannons           = With.units.ours .filter(u => u.aliveAndComplete && u.is(Protoss.PhotonCannon))
    val fighters          = With.units.ours .filter(u => u.canAttack && ! u.unitClass.isWorker && inOurBase(u) && u.remainingCompletionFrames < GameTime(0, 3)())
    lazy val zerglings    = With.units.enemy.filter(u => u.aliveAndComplete && u.is(Zerg.Zergling)  && inOurBase(u))
    lazy val workers      = With.units.ours.filter(u => u.aliveAndComplete && u.unitClass.isWorker)
    lazy val threatening  = zerglings.filter(_.inPixelRadius(32 * 4).exists(n => n.isOurs && n.totalHealth < 200))
    
    if (fighters.isEmpty) {
      return
    }
    if (fighters.size > 4) {
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
    
    val workersNeeded   = 1 + 3 * zerglings.size - 3 * fighters.map(_.unitClass.mineralValue).sum / 100.0
    val workerCap       = workers.size - 4
    val workersToFight  = PurpleMath.clamp(workersNeeded, 0, workerCap)
    val target          = fighters.minBy(zealot => zerglings.map(_.pixelDistanceEdge(zealot)).min).pixelCenter
    
    defenders.get.unitCounter.set(UnitCountExactly(workersToFight.toInt))
    defenders.get.unitPreference.set(UnitPreferClose(target))
    defenders.get.acquire(this)
    defenders.get.units.foreach(_.agent.intend(this, new Intention {
      toTravel = Some(target)
      canFlee = false
    }))
  }
}
