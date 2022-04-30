package Placement.Generation

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Tile, TileRectangle}
import Placement.Templating.Template
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass

object TerranWall {

  def apply(zone: Zone): Option[Fit] = {
    // TODO: Restore this as much as is appropriate
    if ( ! zone.bases.exists(_.isStartLocation) && ! zone.bases.exists(_.naturalOf.exists(_.isStartLocation))) return None

    val exit = zone.exitOriginal
    if (exit.isEmpty) return None
    if (exit.get.radiusPixels > 9 * 32 / 2) return None
    if (zone.edges.exists(e => e != exit.get && e.otherSideof(zone) != With.geography.ourMain.zone && e.otherSideof(zone) != With.geography.ourNatural.zone)) return None
    val searchRadius      = 10
    val searchStart       = exit.get.pixelCenter.tile.subtract(searchRadius, searchRadius)
    val searchEnd         = searchStart.add(2 * searchRadius - 2, 2 * searchRadius - 1)
    val searchArea        = TileRectangle(searchStart, searchEnd)
    val zoneOut           = exit.get.otherSideof(zone)
    val tilesBuildableIn  = zone.tiles.view.filter(_.buildable)
    val tilesBuildableOut = zoneOut.tiles.view.filter(_.buildable)
    val tileIn            = Maff.minBy(tilesBuildableIn.view.filterNot(searchArea.expand(1, 1).contains))(_.groundPixels(exit.get.pixelCenter))
    val tileOut           = Maff.minBy(tilesBuildableOut.view.filterNot(searchArea.expand(1, 1).contains))(_.groundPixels(exit.get.pixelCenter))
    if (tileIn.isEmpty)     return None
    if (tileOut.isEmpty)    return None
    val altitudes         = Seq(tileIn.get.altitude, tileOut.get.altitude).distinct
    val unitsToPlace      = Seq(Seq(Terran.Barracks), Seq(Terran.Barracks, Terran.SupplyDepot), Seq(Terran.Barracks, Terran.SupplyDepot, Terran.SupplyDepot))
    val unitToBlock       = Seq(Zerg.Zergling, Protoss.Zealot, Protoss.Dragoon)
    val trials            = altitudes.flatMap(a => unitsToPlace.view.flatMap(up => unitToBlock.map((a, up, _))))
    trials.map(t => placeAll(t._1, tileIn.get, tileOut.get, searchArea, t._2, Seq.empty, t._3)).find(_.isDefined).flatten
  }

  def placeAll(altitude: Int, tileIn: Tile, tileOut: Tile, area: TileRectangle, unplaced: Seq[UnitClass], placed: Seq[(Tile, UnitClass)], shouldBlock: UnitClass): Option[Fit] = {
    if (unplaced.isEmpty) {
      return if (test(tileIn, tileOut, placed, shouldBlock))
        Some(
          Fit(
            Tile(
              Maff.min(placed.view.map(_._1.x)).getOrElse(0),
              Maff.min(placed.view.map(_._1.y)).getOrElse(0)),
            new Template(placed)))
        else None
    }
    area.tiles
      .filter(_.altitude == altitude)
      .map(t => placeAll(altitude, tileIn, tileOut, area, unplaced.drop(1), placed :+ (t, unplaced.head), shouldBlock))
      .find(_.isDefined).flatten
  }

  def test(tileFrom: Tile, tileTo: Tile, placed: Seq[(Tile, UnitClass)], shouldBlock: UnitClass): Boolean = {
    val areas = placed.map(p => (p._2.tileArea.add(p._1), p._2))
    val unbuildable = areas.view.flatMap(_._1.tiles).find( ! _.buildable)
    if (unbuildable.isDefined) {
      return false
    }
    val intersection = areas.flatMap(a1 => areas.view.filterNot(a1==).map(a2 => (a1._1, a2._1))).find(a => a._1.intersects(a._2))
    if (intersection.isDefined) {
      return false
    }
    val adjacencies = areas.view.flatMap(a1 =>
      a1._1.tiles.flatMap(t1 =>
        areas.view.filterNot(_ == a1).flatMap(a2 => a2._1.tiles.map(t2 =>
          ((t1, a1._2), (t2, a2._2))))))
      .filter(p => Math.abs(p._1._1.x - p._2._1.x) < 2 && Math.abs(p._1._1.y - p._2._1.y) < 2)
    val wideAdjacency = adjacencies.find(a => {
      val as = Seq(a._1, a._2)
      val aLeft   = as.minBy(_._1.x)
      val aRight  = as.maxBy(_._1.x)
      val aTop    = as.minBy(_._1.y)
      val aBottom = as.maxBy(_._1.y)
      // Ideally we'd do this with MATH based on unit properties, so it would generalize to buildings other than Depot/Barracks
      // but I haven't figured how to determine how a building type aligns in the buildtile grid just given its dimensions
      var output = true
      if (aLeft != aRight) {
        if (shouldBlock.width <= Zerg.Zergling.width) {
          output = false
        } else if (shouldBlock.width <= Protoss.Zealot.width) {
          if (aRight._2 == Terran.Barracks) {
            output = false
          }
        }
      }
      if (aTop != aBottom) {
        if (aTop._2 == Terran.Barracks && shouldBlock.width < Protoss.Dragoon.width) {
          output = false
        }
      }
      output
    })
    if (wideAdjacency.isDefined) {
      return false
    }

    val unwalkable = areas.view.flatMap(_._1.tiles).toSet
    val profile = new PathfindProfile(tileFrom, Some(tileTo), alsoUnwalkable = unwalkable)
    val path = profile.find
    if (path.pathExists) {
      return false
    }
    true
  }
}
