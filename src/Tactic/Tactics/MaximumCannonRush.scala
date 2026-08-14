package Tactic.Tactics

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, PixelRectangle, Tile, TileRectangle}
import Mathematics.Shapes.{Pylons, Spiral}
import Micro.Actions.Basic.Build
import Micro.Agency.BuildIntent
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.UnitCounters.CountUpTo
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

  def legalPlacementsOutside(zone: Zone): Set[Tile] = {
    val outsidePerimeter = zone.tiles.view
      .flatMap(_.adjacent4)
      .filter(tile => tile.valid && ! zone.tiles.contains(tile))
      .toSet
    outsidePerimeter
      .flatMap(tile => Seq(tile, tile.left, tile.up, tile.up.left))
      .filter(tile => {
        val tiles = new TileRectangle(tile, 2, 2).tiles
        tiles.forall(t => t.valid && ! zone.tiles.contains(t)) && legal(tile)
      })
  }

  private def unitBox(tile: Tile, unitClass: UnitClass): PixelRectangle = {
    val center = tile.topLeftPixel.add(16 * unitClass.tileWidth, 16 * unitClass.tileHeight)
    PixelRectangle(
      center.subtract(unitClass.dimensionLeft, unitClass.dimensionUp),
      center.add(unitClass.dimensionRightExclusive, unitClass.dimensionDownExclusive))
  }

  private def boxDistance(box: PixelRectangle, pixel: Pixel): Double =
    Maff.broodWarDistanceBox(box.startInclusive, box.endExclusive, pixel, pixel)

  private def farthestBoxDistance(from: PixelRectangle, to: PixelRectangle): Double = Seq(
    to.startInclusive,
    Pixel(to.endExclusive.x, to.startInclusive.y),
    to.endExclusive,
    Pixel(to.startInclusive.x, to.endExclusive.y))
    .map(boxDistance(from, _))
    .max

  def cannonProtectionTiles(cannon: Tile, attacker: UnitClass): Set[Tile] = {
    val cannonRange = Protoss.PhotonCannon.pixelRangeGround
    if (! attacker.attacksGround || attacker.pixelRangeGround > cannonRange) return Set.empty
    val cannonBox = unitBox(cannon, Protoss.PhotonCannon)
    val radius = Math.ceil(cannonRange / 32.0).toInt + 1
    (cannon.x - radius to cannon.x + radius).flatMap(x =>
      (cannon.y - radius to cannon.y + radius).map(Tile(x, _)))
      .filter(tile => new TileRectangle(tile, 2, 2).tiles.forall(_.valid))
      // Both weapons measure range to the same attacker box, so the attacker's dimensions cancel.
      .filter(tile => farthestBoxDistance(cannonBox, unitBox(tile, Protoss.PhotonCannon))
        + attacker.pixelRangeGround <= cannonRange)
      .toSet
  }

  lock
    .setMatcher(Protoss.Probe)
    .setInterruptible(false)

  var caught = false

  override def launch(): Unit = {
    if ( ! With.blackboard.cannonRushMaximum()) return

    caught ||= With.units.ours.exists(u => u.proxied && u.visibleToOpponents)

    val goal = With.units.enemy.find(_.isAny(Terran.Factory, Protoss.RoboticsFacility)).map(_.tile).getOrElse(With.scouting.enemyHome)
    lock
      .setCounter(
        CountUpTo(
          Math.max(
            With.units.countOurs(u => Protoss.Probe(u) && ! u.metro.exists(_.isOurs)),
            1
              + Maff.fromBoolean(With.units.existsEverOurs(_.proxied))
              + Maff.fromBoolean(With.units.existsEverOurs(u =>
                u.proxied
                && u.visibleToOpponents)))))
      .setPreference(PreferClose(goal.center))
      .acquire()

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

    def build(building: UnitClass, tile: Tile): Unit = {
      if (builders.nonEmpty && mineralsLeft >= building.mineralPrice + 72 * With.accounting.ourIncomePerFrameMinerals) {
        mineralsLeft -= building.mineralPrice
        builtUpon ++= new TileRectangle(tile, 2, 2).tiles
        val builder = builders.minBy(b => b.pixelDistanceTravelling(tile.add(1, 1).topLeftPixel) - 64 * Maff.fromBoolean(b.agent.actions.contains(Build.toString)))
        builders -= builder
        builder.intend(this).setScout(Seq.empty).addBuild(BuildIntent(building, tile))
        pylonsMaking += Maff.fromBoolean(building.powers)
        DrawMap.tileRectangle(new TileRectangle(tile, 2, 2), ?(building.powers, Colors.BrightGreen, Colors.BrightBlue))
        DrawMap.label(building.toString, tile.add(1, 1).topLeftPixel)
      }
    }

    def acceptableAnchor(tile: Tile): Boolean = (
      tile.zone != goal.zone
      && legal(tile)
      && Spiral(8).map(tile.add).exists(t =>
        goal.zone.tiles.contains(t)
        && legal(t)
        && t.center.pixelDistance(tile.add(1, 1).topLeftPixel) <= 5 * 32))

    val anchorCannon = ourCannons.find(u => acceptableAnchor(u.tileTopLeft))

    val anchorCannonTile = anchorCannon
      .map(_.tileTopLeft)
      .orElse(Maff.minBy(
        Spiral(25)
          .map(goal.add)
          .filter(acceptableAnchor)
          .filter(buildable(Protoss.Pylon, _)))(t => // We check only Pylon buildability for the anchor because we have no power yet
            t.tileDistanceFast(goal)
            - 0.8 * t.groundTiles(goal)
            - 15 * Maff.fromBoolean(With.grids.scoutingPathsStartLocations(t) > 8)))

    val anchorPylon = ourPylons.find(u => anchorCannonTile.exists(Pylons.powers2(u.tileTopLeft, _)))

    val anchorPylonTile = anchorCannonTile
      .flatMap(cannon =>
        Maff.minBy(
          Pylons
            .tiles2Inverse(cannon)
            .filter(buildable(Protoss.Pylon, _))
            .filter(pylon =>
              pylon.zone != goal.zone
              && legal(pylon)
            && ! new TileRectangle(pylon, 2, 2).intersects(new TileRectangle(cannon, 2, 2))).toVector)(
          _.tileDistanceFast(goal)))

    if (anchorPylon.isEmpty) {
      anchorPylonTile.foreach(build(Protoss.Pylon, _))
    } else if (anchorCannon.isEmpty) {
      anchorCannonTile.foreach(build(Protoss.PhotonCannon, _))
    }

    val hiddenTiles = ?(caught, goal.zone.tiles.filter(t => ! t.visibleToEnemy).toSeq, Seq.empty)
    val seedTiles = hiddenTiles ++ Maff.orElse(
      //ourCannons.filter(_.complete) .map(_.tileTopLeft),
      ourCannons                    .map(_.tileTopLeft),
      anchorCannonTile.toSeq).toVector

    (0 to 2).foreach(i => {

      val safeTiles = seedTiles
        .flatMap(t => Spiral(5).map(t.add))
        .filter(buildable(Protoss.Pylon, _))
        .distinct
        .sortBy(_.add(1, 1).topLeftPixel.pixelDistance(goal.center))

      val poweredTiles        = safeTiles.filter(buildable(Protoss.PhotonCannon, _)).filter(t => t.zone == goal.zone || ourCannons.size < 3)
      val poweredTileDistance = Maff.min(poweredTiles.map(_.add(1, 1).topLeftPixel.pixelDistance(goal.center))).getOrElse(LightYear().toDouble)
      val makePylon           = pylonsMaking < 2 && (poweredTiles.isEmpty || ourCannons.count(_.pixelDistanceCenter(goal.center) < poweredTileDistance) >= 3)
      val building            = ?(makePylon, Protoss.Pylon, Protoss.PhotonCannon)
      val makeTile            = Maff.minBy(
        ?(
          makePylon,
          Maff.orElse(
            safeTiles
              .filter(_.zone == goal.zone),
            seedTiles.flatMap(t => Spiral(5).map(t.add))
              .filter(buildable(Protoss.Pylon, _))
              .filter(_.zone == goal.zone),
            goal.zone.tiles.filter(buildable(Protoss.Pylon, _))),
          poweredTiles))(t =>
        t.add(1, 1).topLeftPixel.pixelDistance(goal.center)
        - 3200 * Maff.fromBoolean( ! makePylon && ourCannons.size < 2 && t.zone != goal.zone))

      if (makeTile.isDefined){
        val building = ?(makePylon, Protoss.Pylon, Protoss.PhotonCannon)
        build(building, makeTile.get)
      }
    })
  }
}
