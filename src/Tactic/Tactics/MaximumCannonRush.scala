package Tactic.Tactics

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.{Pylons, Spiral}
import Micro.Agency.BuildIntent
import ProxyBwapi.Races.Protoss
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

  var probeInside   : FriendlyUnitInfo = null
  var probeOutside  : FriendlyUnitInfo = null

  override def launch(): Unit = {
    if ( ! With.blackboard.cannonRushMaximum()) return

    val goal = With.scouting.enemyHome
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

    lock.units.foreach(_.intend(this).setCanFight(false).toScoutTiles = goal.zone.tiles.toSeq)

    var mineralsLeft  = With.microSlush.available
    var builders      = lock.units
    var builtUpon     = new HashSet[Tile]
    val ourProxies    = With.units.ours.filter(_.proxied)
    val ourPylons     = ourProxies.filter(Protoss.Pylon)
    val ourCannons    = ourProxies.filter(Protoss.PhotonCannon)
    var makingPylon   = ourPylons.exists( ! _.complete)

    def buildable(building: UnitClass, tile: Tile): Boolean = (
      new TileRectangle(tile, 2, 2).tiles.forall(t =>
        ! builtUpon.contains(t)
          && t.buildable
          && t.units.forall(u => u.flying || (u.isOurs && u.unitClass.isWorker)))
        && ( ! building.requiresPsi || With.grids.psi2Height.isSet(tile) || ourPylons.exists(p => Pylons.powers2(p.tileTopLeft, tile))))

    def build(building: UnitClass, tile: Tile): Unit = {
      if (builders.nonEmpty && mineralsLeft >= building.mineralPrice + 72 * With.accounting.ourIncomePerFrameMinerals) {
        mineralsLeft -= building.mineralPrice
        builtUpon ++= new TileRectangle(tile, 2, 2).tiles
        val builder = builders.minBy(_.pixelDistanceTravelling(tile.add(1, 1).topLeftPixel))
        builders -= builder
        builder.intend(this).setScout(Seq.empty).addBuild(BuildIntent(building, tile))
        makingPylon ||= building.powers
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
            - 0.5 * t.groundTiles(goal)
            - 15 * Maff.fromBoolean(With.grids.scoutingPathsStartLocations(t) > 8)))

    val anchorPylon = ourPylons.find(u => anchorCannonTile.exists(Pylons.powers2(u.tileTopLeft, _)))

    val anchorPylonTile = anchorCannonTile
      .flatMap(cannon =>
        Maff.minBy(
          Pylons
            .tiles2Inverse(cannon)
            .filter(buildable(Protoss.Pylon, _))
            .filter(pylon =>
              pylon.zone == goal.zone
              && legal(pylon)

            && ! new TileRectangle(pylon, 2, 2).intersects(new TileRectangle(cannon, 2, 2))).toVector)(
          _.tileDistanceFast(goal)))

    if (anchorPylon.isEmpty) {
      anchorPylonTile.foreach(build(Protoss.Pylon, _))
    } else if (anchorCannon.isEmpty) {
      anchorCannonTile.foreach(build(Protoss.PhotonCannon, _))
    }

    val seedCannons = Maff.orElse(
      ourCannons.filter(_.complete) .map(_.tileTopLeft),
      ourCannons                    .map(_.tileTopLeft),
      anchorCannonTile.toSeq).toVector

    (0 until 2).foreach(i => {

      val safeTiles = seedCannons
        .flatMap(t => Spiral(5).map(t.add))
        .filter(buildable(Protoss.Pylon, _))
        .distinct
        .sortBy(_.add(1, 1).topLeftPixel.pixelDistance(goal.center))

      val poweredTiles  = safeTiles.filter(buildable(Protoss.PhotonCannon, _))
      val cannonTileBestDistance = Maff.min(poweredTiles.map(_.add(1, 1).topLeftPixel.pixelDistance(goal.center))).getOrElse(LightYear().toDouble)
      val makePylon     = ! makingPylon && (poweredTiles.isEmpty || ourCannons.count(_.pixelDistanceCenter(goal.center) < cannonTileBestDistance) >= 3)
      val building      = ?(makePylon, Protoss.Pylon, Protoss.PhotonCannon)
      val makeTile      = Maff.minBy(
        ?(
          makePylon,
          Maff.orElse(
            safeTiles,
            seedCannons.flatMap(t => Spiral(5).map(t.add)).filter(buildable(Protoss.Pylon, _)),
            goal.zone.tiles.filter(buildable(Protoss.Pylon, _))),
          poweredTiles))(_.tileDistanceSquared(goal))

      makeTile.foreach(tile => {
        val building = ?(makePylon, Protoss.Pylon, Protoss.PhotonCannon)
        build(building, tile)
      })
    })
  }
}
