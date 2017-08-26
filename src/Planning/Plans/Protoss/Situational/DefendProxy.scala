package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Micro.Squads.Goals.SquadProtectZone
import Micro.Squads.Squad
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

class DefendProxy extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWorkers)
  
  var lastProxyCount = 0
  
  override def onUpdate() {
  
    val proxies = getProxies.sortBy(_.totalHealth).sortBy(_.remainingBuildFrames)
    val squad = new Squad(this)
    
    if (proxies.isEmpty) return
    
    //TODO: Choose based on HP and remaining build time
    //However, our remaining build time for enemy units depends on HP, and doesn't yet correctly identify damage taken.
    val workersRequired = proxies.map(proxy =>
      (
        proxy,
        if (proxy.canAttack)
          if (proxy.complete)
            6
          else
            4
        else
          3
      )).toMap
    val totalWorkersRequired = workersRequired.values.sum
    val maxWorkers = With.units.ours.count(_.unitClass.isWorker) - 5
    val finalWorkers = Math.min(totalWorkersRequired, maxWorkers)
    defenders.get.unitCounter.set(new UnitCountBetween(0, finalWorkers))
    defenders.get.unitPreference.set(UnitPreferClose(proxies.head.pixelCenter))
    if (lastProxyCount > proxies.size) {
      defenders.get.release()
    }
    defenders.get.acquire(this)
  
    squad.enemies = proxies
    squad.conscript(defenders.get.units)
    squad.goal = new SquadProtectZone(proxies.head.zone)
  }
  
  private def getProxies: Seq[UnitInfo] = {
    With.units.enemy.toSeq.filter(e => scaryTypes.contains(e.unitClass) && isProxied(e) && ! e.flying)
  }
  
  private def isProxied(enemy: UnitInfo): Boolean = {
    val location = enemy.pixelCenter
    val thresholdDistance = 32.0 * 50.0
    ! location.zone.owner.isEnemy &&
    (
      location.zone.owner.isUs ||
      With.geography.ourBases.map(_.heart.pixelCenter).exists(p =>
        p.pixelDistanceFast (location)  < thresholdDistance &&
        p.groundPixels      (location)  < thresholdDistance)
    )
  }
  
  lazy val scaryTypes = Vector(
    Terran.Bunker,
    Terran.Barracks,
    Terran.Factory,
    Terran.Refinery,
    Protoss.Assimilator,
    Protoss.Pylon,
    Protoss.PhotonCannon,
    Zerg.CreepColony,
    Zerg.Extractor,
    Zerg.SunkenColony,
    Zerg.Hatchery
  )
}
