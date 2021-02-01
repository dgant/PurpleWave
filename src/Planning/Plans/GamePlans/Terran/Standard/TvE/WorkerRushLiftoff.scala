package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.BuildRequests.Get
import Mathematics.Points.{Pixel, Tile}
import Micro.Agency.Intention
import Planning.Plans.Army.AttackWithWorkers
import Planning.Plans.Basic.Do
import Planning.Plans.Compound._
import Planning.Plans.Macro.BuildOrders.{BuildOrder, FollowBuildOrder}
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchComplete, UnitMatchWorkers}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class WorkerRushLiftoff extends Parallel {
  
  children.set(Vector(
    new AttackWithWorkers,
    new BuildOrder(Get(5, Terran.SCV)),
    new Do(() => new FollowBuildOrder().update())
  ))
  
  val dyingLock = new LockUnits
  dyingLock.unitMatcher.set((unit) => unit.totalHealth < 21)
  dyingLock.unitCounter.set(UnitCountEverything)
  
  val flyerLock = new LockUnits
  flyerLock.unitMatcher.set(Terran.CommandCenter)
  flyerLock.unitCounter.set(UnitCountEverything)
  
  val hiderLock = new LockUnits
  hiderLock.unitMatcher.set(UnitMatchWorkers)
  hiderLock.unitCounter.set(UnitCountEverything)
  
  private var bestGroundPixel: Pixel = _
  private var bestAirPixel: Pixel = _
  
  var finishedTraining = false
  override def onUpdate() {
    if (With.units.enemy.exists(_.visible)
      || bestGroundPixel == null
      || bestAirPixel == null)
    pickBestGroundPixel()
    pickBestAirPixel()
    
    if (With.units.countOurs(UnitMatchAnd(Terran.SCV, UnitMatchComplete)) >= 5) {
      finishedTraining = true
    }
    if (finishedTraining) {
      flyerLock.acquire(this)
      flyerLock.units.foreach(flyAway)
    }
    
    dyingLock.acquire(this)
    dyingLock.units.foreach(runAway)
    
    if ( ! With.blackboard.enemyUnitDied) {
      super.onUpdate()
      return
    }
  
    // We want units to be able to flee
    With.blackboard.yoloEnabled.set(false)
    
    hiderLock.acquire(this)
    hiderLock.units.foreach(runAway)
    
    // TODO: Spam chat
  }
  
  private def runAway(unit: FriendlyUnitInfo) {
    val intent = new Intention {
      canFight = false
      toTravel = Some(bestGroundPixel)
    }
    unit.agent.intend(this, intent)
  }
  
  private def flyAway(unit: FriendlyUnitInfo) {
    val intent = new Intention {
      canFight = false
      canLiftoff = true
      toTravel = Some(bestAirPixel)
    }
    unit.agent.intend(this, intent)
  }
  
  private lazy val baseZones = With.geography.bases.map(_.zone)
  private lazy val baseZonePaths =
    baseZones.flatMap(zone1 =>
      baseZones.flatMap(zone2 =>
        With.paths.zonePath(zone1, zone2)))
  private lazy val zoneCrossings = With.geography.zones
    .map(zone =>
    (
      zone,
      baseZonePaths.count(_.steps.exists(step => step.from == zone || step.to == zone))
    ))
    .toMap
  
  private def evaluateZone(zone: Zone, allowIsland: Boolean): Double = {
    if (zone.island && ! allowIsland) return Double.PositiveInfinity
    val scoreCrossing   = 1.0 + zoneCrossings.getOrElse(zone, 2)
    val scoreBase       = 1.0 + zone.bases.size
    val scoreStarting   = 1.0 + zone.bases.count(_.isStartLocation)
    val scoreNatural    = 1.0 + zone.bases.count(_.isNaturalOf.isDefined)
    val scoreVisible    = 1.0 + zone.units.count(_.isEnemy)
    val scoreDistance   = 1.0 + ByOption.min(With.units.enemy.view.filter(_.canMove).map(_.framesToTravelTo(zone.centroid.pixelCenter))).getOrElse(0)

    val scoreTotal      = scoreCrossing * scoreBase * scoreStarting * scoreNatural * scoreVisible / scoreDistance
    scoreTotal
  }
  
  private def pickBestGroundPixel() {
    val tiles = With.geography.allTiles
      .filter(With.grids.walkable.get)
      .filter(tile => With.paths.zonePath(With.self.startTile.zone, tile.zone).isDefined)
      .map(tile => (tile, evaluateGroundTile(tile)))
      .toMap
    // TODO: Priority queue and eliminate unpathable tiles
    val output = tiles.maxBy(_._2)._1
    bestGroundPixel = output.pixelCenter
  }
  
  private def pickBestAirPixel() {
    val tiles = With.geography.allTiles
      .map(tile => (tile, evaluateAirTile(tile)))
      .toMap
    val output = tiles.maxBy(_._2)._1
    bestAirPixel = output.pixelCenter
  }
  
  private def evaluateGroundTile(tile: Tile): Double = {
    val zone = tile.zone
    val scoreZoneSize   = 1.0 + zone.tiles.size
    val scoreDistance   = 1.0 + ByOption.min(With.units.enemy.filter(_.canMove).map(_.framesToTravelTo(zone.centroid.pixelCenter).toDouble)).getOrElse(With.scouting.mostBaselikeEnemyTile.tileDistanceFast(tile))
    val scoreObscurity  = 1.0 + tile.zone.edges.map(_.pixelCenter.pixelDistance(tile.pixelCenter)).sum
    val scoreCrossing   = 1.0 + zoneCrossings.getOrElse(zone, 2)
    val scoreBase       = 1.0 + zone.bases.size
    val scoreStarting   = 1.0 + zone.bases.count(_.isStartLocation)
    val scoreNatural    = 1.0 + zone.bases.count(_.isNaturalOf.isDefined)
    val scoreVisible    = 1.0 + zone.units.count(_.isEnemy)
    val scoreCentrality = 1.0 + tile.tileDistanceFromEdge
    val scoreTotal      = scoreZoneSize * scoreDistance * scoreObscurity / scoreCrossing / scoreBase / scoreStarting / scoreNatural / scoreVisible / scoreCentrality
    scoreTotal
  }
  
  private def evaluateAirTile(tile: Tile): Double = {
    val scoreIsland     = if (tile.zone.island || tile.zone.unwalkable) 1000000.0 else 1.0
    val scoreObscurity  = 1.0 + tile.zone.edges.map(_.pixelCenter.pixelDistance(tile.pixelCenter)).sum
    val scoreCentrality = 1.0 + tile.tileDistanceFromEdge
    val scoreAltitude   = 1.0 + tile.altitude
    val scoreTotal      = scoreIsland * scoreAltitude *  scoreObscurity / scoreCentrality
    scoreTotal
  }
}