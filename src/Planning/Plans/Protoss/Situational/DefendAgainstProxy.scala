package Planning.Plans.Protoss.Situational

import Information.Geography.Types.Base
import Lifecycle.With
import Micro.Squads.Goals.SquadPush
import Micro.Squads.Squad
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class DefendAgainstProxy extends Plan {
  
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
    squad.goal = new SquadPush(proxies.head.pixelCenter)
  }
  
  private def getProxies: Seq[UnitInfo] = {
    With.units.enemy.toSeq.filter(e => scaryTypes.contains(e.unitClass) && isProxied(e) && ! e.flying)
  }
  
  private def isProxied(enemy: UnitInfo): Boolean = {
    val pixel                     = enemy.pixelCenter
    val thresholdDistance         = 32.0 * 50.0
    def baseDistance(base: Base)  = base.heart.pixelCenter.pixelDistanceFast(pixel)
    lazy val closestEnemyBase     = ByOption.minBy(With.geography.enemyBases)(_.heart.pixelCenter.pixelDistanceFast(pixel))
    lazy val closestOurBase       = ByOption.minBy(With.geography.ourBases)(_.heart.pixelCenter.pixelDistanceFast(pixel))
    lazy val enemyBaseDistance    = closestEnemyBase.map(baseDistance)
    lazy val ourBaseDistance      = closestEnemyBase.map(baseDistance)
    lazy val withinOurThreshold   = ourBaseDistance.exists(_ < thresholdDistance)
    lazy val closerToUs           = enemyBaseDistance.exists(distanceEnemy => ourBaseDistance.exists(distanceOurs => distanceOurs < distanceEnemy))
    val output = closerToUs && withinOurThreshold
    output
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
