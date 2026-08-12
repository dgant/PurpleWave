package Tactic.Tactics

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.{Pylons, Spiral}
import Micro.Actions.Basic.Build
import Micro.Agency.BuildIntent
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.UnitCounters.CountExactly
import Utilities.UnitFilters.IsBuilding
import Utilities.UnitPreferences.PreferClose
import Utilities.{?, LightYear}

import scala.collection.immutable.HashSet

class MaximumCannonRush extends Tactic {

  def legalOneTile(tile: Tile): Boolean = tile.buildableTerrain
  def legal(tile: Tile): Boolean = (
    legalOneTile(tile)
    && legalOneTile(tile.add(1, 1))
    && legalOneTile(tile.add(1, 0))
    && legalOneTile(tile.add(0, 1)))

  lock
    .setCounter(CountExactly(3))
    .setMatcher(Protoss.Probe)
    .setInterruptible(false)

  var caught = false
  var anchorCannonTilePlanned: Option[Tile] = None
  var anchorSupportCannonTilePlanned: Option[Tile] = None

  override def launch(): Unit = {
    if ( ! With.blackboard.cannonRushMaximum()) return

    caught ||= With.units.ours.exists(u => u.proxied && u.visibleToOpponents)

    val goal = With.units.enemy.find(_.isAny(Terran.Factory, Protoss.RoboticsFacility)).map(_.tile).getOrElse(With.scouting.enemyHome)
    lock.setPreference(PreferClose(goal.center))
    lock.acquire()

    if (lock.units.isEmpty) return

    if ( ! With.units.enemy.exists(IsBuilding)) {
      With.tactics.workerScout.allocateScouts(
        Maff
          .takeN(lock.units.size, With.scouting.baseIntrigue)(Ordering.by(_._2))
          .map(_._1),
        lock.units.toVector)
      return
    }

    lock.units.foreach(_.intend(this)
      .setCanFight(false)
      .setCanFlee( ! With.units.existsEnemy(Terran.Marine, Protoss.Dragoon, Zerg.Hydralisk, Zerg.Mutalisk))
      .setScout(goal.zone.tiles.toSeq))

    var mineralsLeft  = With.microSlush.available
    var builders      = lock.units
    var builtUpon     = new HashSet[Tile]
    val ourProxies    = With.units.ours.filter(_.proxied)
    val ourPylons     = ourProxies.filter(Protoss.Pylon)
    val ourCannons    = ourProxies.filter(Protoss.PhotonCannon)
    var pylonsMaking  = ourPylons.count( ! _.complete)

    def buildable(building: UnitClass, tile: Tile): Boolean = (
      new TileRectangle(tile, 2, 2).tiles.forall(t =>
        ! builtUpon.contains(t)
        && t.buildable
        && t.units.forall(u => u.flying || (u.isOurs && u.unitClass.isWorker))
        && ! t.enemiesAttackingGround.exists(_.unitClass.isBuilding))
      && ( ! building.requiresPsi || With.grids.psi2Height.isSet(tile) || ourPylons.exists(p => Pylons.powers2(p.tileTopLeft, tile))))

    def enemiesCovering(tile: Tile) = new TileRectangle(tile, 2, 2).tiles.flatMap(_.enemiesAttackingGround).distinct
    def safeToStart(building: UnitClass, tile: Tile) = building.powers || enemiesCovering(tile).forall(enemy =>
      ! enemy.canAttack
        || enemy.unitClass.isWorker
        || ourCannons.exists(c => c.complete && c.inRangeToAttack(enemy)))
    def constructionStations(tile: Tile) = new TileRectangle(tile, 2, 2).tilesSurrounding.filter(t =>
      t.walkable && ! t.units.exists(u => u.unitClass.isBuilding && ! u.flying))
    def approach(builder: FriendlyUnitInfo, tile: Tile) = Maff.minBy(constructionStations(tile))(builder.pixelDistanceTravelling).map(_.center)
    def prepare(building: UnitClass, tile: Tile, excluded: Set[Int] = Set.empty): Option[Int] = {
      Maff.minBy(builders.filterNot(builder => excluded.contains(builder.id)))(_.pixelDistanceTravelling(tile.add(1, 1).topLeftPixel)).map(builder => {
        builder.intend(this).setScout(Seq.empty).addBuild(BuildIntent(building, tile, startNow = false, approach(builder, tile)))
        builder.id
      })
    }
    def preparePair(first: Tile, second: Tile): Unit = {
      val firstBuilder = prepare(Protoss.PhotonCannon, first)
      prepare(Protoss.PhotonCannon, second, firstBuilder.toSet)
    }
    def build(building: UnitClass, tile: Tile): Unit = {
      if (builders.nonEmpty && safeToStart(building, tile) && mineralsLeft >= building.mineralPrice) {
        mineralsLeft -= building.mineralPrice
        builtUpon ++= new TileRectangle(tile, 2, 2).tiles
        val builder = builders.minBy(b => b.pixelDistanceTravelling(tile.add(1, 1).topLeftPixel) - 64 * Maff.fromBoolean(b.agent.actions.contains(Build.toString)))
        builders -= builder
        builder.intend(this).setScout(Seq.empty).addBuild(BuildIntent(building, tile, approach = approach(builder, tile)))
        pylonsMaking += Maff.fromBoolean(building.powers)
        DrawMap.tileRectangle(new TileRectangle(tile, 2, 2), ?(building.powers, Colors.BrightGreen, Colors.BrightBlue))
        DrawMap.label(building.toString, tile.add(1, 1).topLeftPixel)
      }
    }

    def tilesAdvancedInto(tile: Tile) = Spiral(8).map(tile.add).filter(t =>
      goal.zone.tiles.contains(t)
        && legal(t)
        && t.add(1, 1).topLeftPixel.pixelDistance(tile.add(1, 1).topLeftPixel) <= Protoss.PhotonCannon.pixelRangeGround).toVector
    def acceptableAnchor(tile: Tile): Boolean = tile.zone != goal.zone && legal(tile) && tilesAdvancedInto(tile).nonEmpty
    def supportsAnchor(tile: Tile, anchor: Tile): Boolean = (
      tile.zone != goal.zone
        && legal(tile)
        && ! new TileRectangle(tile, 2, 2).intersects(new TileRectangle(anchor, 2, 2))
        && tile.add(1, 1).topLeftPixel.pixelDistance(anchor.add(1, 1).topLeftPixel) <= Protoss.PhotonCannon.pixelRangeGround)
    def hiddenFromEnemy(tile: Tile): Boolean = new TileRectangle(tile, 2, 2).tiles.forall( ! _.visibleToEnemy)
    def reachable(tile: Tile): Boolean = new TileRectangle(tile, 2, 2).tilesSurrounding.exists(t =>
      t.walkable && lock.units.exists(_.pixelDistanceTravelling(t.center) < LightYear()))
    def anchorScore(tile: Tile): Double = {
      val distance = tile.tileDistanceFast(goal)
      val exposure = new TileRectangle(tile, 2, 2).tilesSurrounding.count(_.walkable)
      if (caught) {
        distance - 4 * Maff.fromBoolean(hiddenFromEnemy(tile)) + 2 * exposure
      } else {
        val travel = tile.groundTiles(goal)
        val scoutingDistance = With.grids.scoutingPathsStartLocations(tile)
        (0.75 * distance
          - Math.min(96, travel)
          - 1.5 * Math.min(16, scoutingDistance)
          - 8 * Maff.fromBoolean(hiddenFromEnemy(tile))
          + 2 * exposure
          + 8 * Math.max(0, 16 - distance))
      }
    }
    def pylonPowersCannon(pylon: Tile, cannon: Tile) = (
      ! new TileRectangle(pylon, 2, 2).intersects(new TileRectangle(cannon, 2, 2))
        && Pylons.powers2(pylon, cannon))
    def availableAnchorPylonTiles(cannon: Tile) = Pylons
      .tiles2Inverse(cannon)
      .filter(buildable(Protoss.Pylon, _))
      .filter(legal)
      .filter(reachable)
      .filter(pylonPowersCannon(_, cannon)).toVector
    def viableAnchorPower(cannon: Tile) = ourPylons.exists(pylon => pylonPowersCannon(pylon.tileTopLeft, cannon)) || availableAnchorPylonTiles(cannon).nonEmpty
    def pylonEdgesSharedWithCannon(pylon: Tile, cannon: Tile): Int = {
      val pylonRectangle = new TileRectangle(pylon, 2, 2)
      new TileRectangle(cannon, 2, 2).tiles.flatMap(_.adjacent4).count(pylonRectangle.contains)
    }
    def anchorPylonScore(pylon: Tile, cannon: Tile): Double = (
      pylon.tileDistanceFast(goal)
        - 10 * Maff.fromBoolean(pylon.add(1, 1).topLeftPixel.pixelDistance(goal.center) < cannon.add(1, 1).topLeftPixel.pixelDistance(goal.center))
        - 20 * pylonEdgesSharedWithCannon(pylon, cannon)
        - 4 * Maff.fromBoolean(hiddenFromEnemy(pylon))
        + 2 * pylon.tileDistanceFast(cannon))

    val availableAnchorTilesAll = goal.zone.tiles
      .flatMap(tile => Spiral(8).map(tile.add))
      .filter(_.valid)
      .filter(acceptableAnchor)
      .filter(buildable(Protoss.Pylon, _))
      .filter(reachable)
      .filter(viableAnchorPower)
    val enemyNatural = With.scouting.enemyMain.flatMap(_.natural)
    def inEnemyNatural(tile: Tile) = enemyNatural.exists(natural => tile.zone == natural.zone || tile.base.contains(natural))
    val availableAnchorTiles = Maff.orElse(
      availableAnchorTilesAll.filterNot(inEnemyNatural),
      availableAnchorTilesAll)
    val existingAnchorCannons = ourCannons.filter(cannon => acceptableAnchor(cannon.tileTopLeft))
    val anchorCannonTile = existingAnchorCannons
      .find(cannon => anchorCannonTilePlanned.contains(cannon.tileTopLeft))
      .orElse(existingAnchorCannons.headOption)
      .map(_.tileTopLeft)
      .orElse(anchorCannonTilePlanned.filter(availableAnchorTilesAll.contains))
      .orElse(Maff.minBy(availableAnchorTiles)(anchorScore))
    anchorCannonTilePlanned = anchorCannonTile

    val anchorCannon = anchorCannonTile.flatMap(tile => ourCannons.find(_.tileTopLeft == tile))
    val anchorPylon = ourPylons.find(u => anchorCannonTile.exists(pylonPowersCannon(u.tileTopLeft, _)))
    val anchorPylonTile = anchorCannonTile.flatMap(cannon => Maff.minBy(availableAnchorPylonTiles(cannon))(anchorPylonScore(_, cannon)))

    val anchorSupportCannon = ourCannons.find(cannon => anchorCannonTile.exists(supportsAnchor(cannon.tileTopLeft, _)))
    val availableAnchorSupportCannonTiles = anchorCannonTile.toSeq.flatMap(anchor => Spiral(8)
      .map(anchor.add)
      .filter(supportsAnchor(_, anchor))
      .filter(buildable(Protoss.PhotonCannon, _)))
    val anchorSupportCannonTile = anchorSupportCannon
      .map(_.tileTopLeft)
      .orElse(anchorSupportCannonTilePlanned.filter(availableAnchorSupportCannonTiles.contains))
      .orElse(Maff.minBy(availableAnchorSupportCannonTiles)(anchorScore))
    anchorSupportCannonTilePlanned = anchorSupportCannonTile

    if (anchorPylon.isEmpty) {
      anchorPylonTile.foreach(build(Protoss.Pylon, _))
      return
    } else if (anchorCannon.isEmpty) {
      val buildPair = caught && anchorSupportCannonTile.isDefined && builders.size >= 2
      if (buildPair && mineralsLeft < 2 * Protoss.PhotonCannon.mineralPrice) {
        preparePair(anchorCannonTile.get, anchorSupportCannonTile.get)
      } else {
        anchorCannonTile.foreach(build(Protoss.PhotonCannon, _))
        if (buildPair) anchorSupportCannonTile.foreach(build(Protoss.PhotonCannon, _))
      }
      return
    } else if (anchorSupportCannon.isEmpty && anchorSupportCannonTile.isDefined && ourCannons.count(_.complete) < 2) {
      anchorSupportCannonTile.foreach(tile => {
        prepare(Protoss.PhotonCannon, tile)
        build(Protoss.PhotonCannon, tile)
      })
      return
    }

    if (ourCannons.count(_.complete) < 1 || ourCannons.size < 2) return

    val completedCannonFrontDistance = Maff.min(ourCannons.filter(_.complete).map(_.pixelDistanceCenter(goal.center))).getOrElse(LightYear().toDouble)
    val stagingCannon = Maff.minBy(ourCannons.filter( ! _.complete))(_.pixelDistanceCenter(goal.center))
      .filter(_.pixelDistanceCenter(goal.center) < completedCannonFrontDistance)
    val advanceZones = Set(goal.zone) ++ anchorCannonTile.map(_.zone)
    val hiddenTiles = ?(caught, goal.zone.tiles.filter(hiddenFromEnemy).toSeq, Seq.empty)
    val seedTiles = hiddenTiles ++ ourCannons.map(_.tileTopLeft) ++ anchorCannonTile.toSeq ++ ourPylons.map(_.tileTopLeft)

    (0 to 2).foreach(i => {

      val safeTiles = (seedTiles
        .flatMap(t => Spiral(5).map(t.add)) ++ ourCannons
        .filter(_.complete)
        .flatMap(c => Spiral(8).map(c.tileTopLeft.add).filter(t =>
          t.add(1, 1).topLeftPixel.pixelDistance(c.tileTopLeft.add(1, 1).topLeftPixel) <= Protoss.PhotonCannon.pixelRangeGround)))
        .filter(t => advanceZones.contains(t.zone))
        .filter(buildable(Protoss.Pylon, _))
        .toVector
        .distinct
      def tileExposure(tile: Tile): Int = new TileRectangle(tile, 2, 2).tilesSurrounding.count(t =>
        t.walkable
          && ! builtUpon.contains(t)
          && ! t.units.exists(u => u.unitClass.isBuilding && ! u.flying))
      def cannonProtection(tile: Tile): Int = ourCannons.count(c =>
        c.complete
          && tile.add(1, 1).topLeftPixel.pixelDistance(c.tileTopLeft.add(1, 1).topLeftPixel) <= Protoss.PhotonCannon.pixelRangeGround)
      def pylonShields(tile: Tile): Int = ourPylons.count(p =>
        p.tileTopLeft.add(1, 1).topLeftPixel.pixelDistance(goal.center) < tile.add(1, 1).topLeftPixel.pixelDistance(goal.center)
          && p.tileTopLeft.add(1, 1).topLeftPixel.pixelDistance(tile.add(1, 1).topLeftPixel) <= 5 * 32)
      def pylonWallsForCannons(tile: Tile): Int = ourCannons.count(c =>
        tile.add(1, 1).topLeftPixel.pixelDistance(goal.center) < c.pixelDistanceCenter(goal.center)
          && tile.add(1, 1).topLeftPixel.pixelDistance(c.tileTopLeft.add(1, 1).topLeftPixel) <= 5 * 32)
      val poweredTilesAll              = safeTiles.filter(buildable(Protoss.PhotonCannon, _)).filter(safeToStart(Protoss.PhotonCannon, _))
      val poweredTilesForward          = poweredTilesAll.filter(_.zone == goal.zone)
      val poweredTilesProtected        = poweredTilesAll.filter(cannonProtection(_) > 0)
      val poweredTilesProtectedForward = poweredTilesProtected.filter(_.zone == goal.zone)
      val cannonFrontDistance          = Maff.min(ourCannons.filter(_.complete).map(_.pixelDistanceCenter(goal.center))).getOrElse(LightYear().toDouble)
      val poweredTilesProtectedAdvance = poweredTilesProtected.filter(_.add(1, 1).topLeftPixel.pixelDistance(goal.center) < cannonFrontDistance)
      val poweredTiles                 = Maff.orElse(
        poweredTilesProtectedForward,
        poweredTilesProtectedAdvance,
        poweredTilesProtected,
        poweredTilesForward,
        poweredTilesAll)
      val poweredTileDistance = Maff.min(poweredTiles.map(_.add(1, 1).topLeftPixel.pixelDistance(goal.center))).getOrElse(LightYear().toDouble)
      val frontNeedsShield    = (caught
        && poweredTilesProtectedAdvance.nonEmpty
        && Maff.minBy(ourCannons.filter(_.complete))(_.pixelDistanceCenter(goal.center)).exists(c => pylonShields(c.tileTopLeft) == 0))
      val makePylon           = pylonsMaking < 2 && (frontNeedsShield || poweredTiles.isEmpty || ourCannons.size >= 4 && poweredTilesProtectedAdvance.isEmpty || ourCannons.size >= 3 * ourPylons.count(_.complete) || ourCannons.count(_.pixelDistanceCenter(goal.center) < poweredTileDistance) >= 3)
      val protectedPylonTiles = ourCannons
        .filter(_.complete)
        .flatMap(c => Spiral(8).map(c.tileTopLeft.add).filter(t =>
          t.add(1, 1).topLeftPixel.pixelDistance(c.tileTopLeft.add(1, 1).topLeftPixel) <= Protoss.PhotonCannon.pixelRangeGround))
        .filter(buildable(Protoss.Pylon, _))
        .toVector
        .distinct
      val shieldingPylonTiles = protectedPylonTiles.filter(pylonWallsForCannons(_) > 0)
      val advancingPylonTiles = safeTiles.filter(pylon => Pylons.tiles2(pylon).exists(cannon =>
        ! new TileRectangle(pylon, 2, 2).intersects(new TileRectangle(cannon, 2, 2))
          && buildable(Protoss.Pylon, cannon)
          && (cannon.zone == goal.zone || cannon.add(1, 1).topLeftPixel.pixelDistance(goal.center) < cannonFrontDistance)))
      val availableTiles = ?(
        makePylon,
        Maff.orElse(
          ?(poweredTilesProtectedAdvance.isEmpty, advancingPylonTiles, Seq.empty),
          ?(frontNeedsShield, shieldingPylonTiles, Seq.empty),
          protectedPylonTiles,
          safeTiles,
          seedTiles.flatMap(t => Spiral(5).map(t.add))
            .filter(buildable(Protoss.Pylon, _))
            .filter(t => advanceZones.contains(t.zone)),
          advanceZones.flatMap(_.tiles).filter(buildable(Protoss.Pylon, _))),
        poweredTiles)
      val makeTile = Maff.minBy(availableTiles)(t =>
        t.add(1, 1).topLeftPixel.pixelDistance(goal.center)
          + ?(makePylon, 16, 24) * tileExposure(t)
          - ?(makePylon, 128 * pylonWallsForCannons(t), 64 * cannonProtection(t) + 96 * pylonShields(t))
          - 640 * Maff.fromBoolean( ! makePylon && t.zone == goal.zone && hiddenFromEnemy(t))
          - 3200 * Maff.fromBoolean( ! makePylon && ourCannons.size < 2 && ! advanceZones.contains(t.zone)))

      makeTile.foreach(tile => {
        val building = ?(makePylon, Protoss.Pylon, Protoss.PhotonCannon)
        val waitForCannonPair = (caught
          && ! makePylon
          && poweredTilesProtectedAdvance.size >= 2
          && stagingCannon.isEmpty
          && builders.size >= 2
          && mineralsLeft < 2 * Protoss.PhotonCannon.mineralPrice)
        if (waitForCannonPair) {
          prepare(building, tile)
        } else {
          build(building, tile)
        }
      })
    })
  }
}
