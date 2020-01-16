package Planning.Plans.GamePlans.Protoss.Situational

import Information.Geography.Types.Base
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Squads.Goals.GoalRazeProxies
import Micro.Squads.Squad
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountBetween
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchWarriors, UnitMatchWorkers}
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Property}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class DefendAgainstProxy extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchOr(UnitMatchWorkers, UnitMatchWarriors))
  
  var lastProxyCount = 0
  
  override def onUpdate() {
  
    if (With.frame > GameTime(5, 0)()) {
      return
    }
    
    val proxies = getProxies.sortBy(_.totalHealth).sortBy(_.remainingCompletionFrames)
    val squad = new Squad(this)

    def unitsNearby(proxy: UnitInfo) = With.units.inTileRadius(proxy.tileIncludingCenter, 8)
    def enemiesNearby(proxy: UnitInfo) = unitsNearby(proxy).filter(_.isEnemy)
    
    if (proxies.isEmpty) return
    
    //TODO: Choose based on HP and remaining build time
    //However, our remaining build time for enemy units depends on HP, and doesn't yet correctly identify damage taken.
    val workersRequired = proxies.map(proxy =>
      (
        proxy,
        if (enemiesNearby(proxy).exists(u => u.unitClass.isBuilding && u.canAttack))
          // Don't pull new units in to attack cannons
          unitsNearby(proxy).count(u => u.isOurs && u.canAttack)
        else if (With.frame < GameTime(3, 15)()
          && proxy.is(Protoss.Pylon)
          && enemiesNearby(proxy).exists(_.is(Protoss.Gateway)))
          6
        else if (proxy.isBunker() && (proxy.complete || ! proxy.matchups.allies.exists(a => a.unitClass.isWorker && a.pixelDistanceEdge(proxy) < 256)))
          0
        else if (proxy.attacksAgainstGround > 0)
          4
        else if (proxy.unitClass.isGas)
          (if (With.enemy.isProtoss) 0 else 3) // We have PvP gas steal reactions but none yet for other races
        else
          1 // We shouldn't pull for other buildings in general; this is mostly just to keep eyes on them
      )).toMap
    val totalWorkersRequired  = workersRequired.values.sum
    val maxWorkers            = With.units.countOurs(UnitMatchWorkers) - 5
    val finalWorkers          = Math.min(totalWorkersRequired, maxWorkers)
    val squadDestination      = proxies.head.pixelCenter

    if (totalWorkersRequired <= 0) {
      defenders.get.release()
      return
    }
    defenders.get.unitCounter.set(new UnitCountBetween(0, finalWorkers))
    defenders.get.unitPreference.set(UnitPreferClose(squadDestination))
    if (lastProxyCount > proxies.size) {
      defenders.get.release()
    }
    lastProxyCount = proxies.size
    defenders.get.acquire(this)
  
    squad.enemies = proxies
    With.squads.commission(squad)
    defenders.get.units.foreach(squad.recruit)
    squad.setGoal(new GoalRazeProxies(squadDestination))
  }
  
  private def getProxies: Seq[UnitInfo] = {
    With.units.enemy.toSeq.filter(e =>
      e.likelyStillAlive
      && e.possiblyStillThere
      && scaryTypes.contains(e.unitClass)
      && isProxied(e)
      && ! e.flying)
  }
  
  private def isProxied(enemy: UnitInfo): Boolean = {
    val pixel                     = enemy.pixelCenter
    val thresholdDistance         = 32.0 * 50.0
    def baseDistance(base: Base)  = base.heart.pixelCenter.groundPixels(pixel)
    lazy val closestEnemyBase     = ByOption.minBy(With.geography.enemyBases)(_.heart.pixelCenter.groundPixels(pixel))
    lazy val closestOurBase       = ByOption.minBy(With.geography.ourBases)(_.heart.pixelCenter.groundPixels(pixel))
    lazy val enemyBaseDistance    = closestEnemyBase.map(baseDistance).getOrElse(With.geography.startBases.map(baseDistance).max)
    lazy val ourBaseDistance      = closestOurBase.map(baseDistance).getOrElse(Double.PositiveInfinity)
    lazy val withinOurThreshold   = ourBaseDistance < thresholdDistance
    lazy val closerToUs           = ourBaseDistance < enemyBaseDistance
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
