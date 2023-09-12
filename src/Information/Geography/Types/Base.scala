package Information.Geography.Types

import Lifecycle.With
import Mathematics.Points.{Direction, Points, Tile, TileRectangle}
import Mathematics.{Maff, Shapes}
import Performance.Cache
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?
import Utilities.Time.{Forever, Minutes}

final class Base(val name: String, val townHallTile: Tile, val tiles: Set[Tile]) extends Geo {
  lazy val metro              : Metro             = With.geography.metros.find(_.bases.contains(this)).get
  val townHallArea            : TileRectangle     = Protoss.Nexus.tileArea.add(townHallTile)
  val zone                    : Zone              = With.geography.zoneByTile(townHallTile)
  val bases                   : Vector[Base]      = Vector(this)
  val zones                   : Vector[Zone]      = Vector(zone)
  val economicValue           : Cache[Double]     = new Cache(() => units.view.filter(u => u.unitClass.isBuilding || u.unitClass.isWorker).map(_.subjectiveValue).sum)
  var natural                 : Option[Base]      = None
  var naturalOf               : Option[Base]      = None
  var townHall                : Option[UnitInfo]  = None
  var units                   : Vector[UnitInfo]  = Vector.empty
  var gas                     : Vector[UnitInfo]  = Vector.empty
  var minerals                : Vector[UnitInfo]  = Vector.empty
  var owner                   : PlayerInfo        = With.neutral
  val saturation              : Cache[Double]     = new Cache(() => workerCount.toDouble / (1 + 3 * gas.size + 2 * minerals.size))
  var allTimeOwners           : Set[PlayerInfo]   = Set.empty
  var enemyCombatValue        : Double            = _
  var workerCount             : Int               = _
  var mineralsLeft            : Int               = 0
  var gasLeft                 : Int               = 0
  var startingMinerals        : Int               = 0
  var startingGas             : Int               = 0
  var lastPlannedExpo         : Int               = - Forever()
  var lastFrameScoutedByUs    : Int               = 0
  var lastFrameScoutedByEnemy : Int               = 0
  var frameTaken              : Int               = 0
  def isOurMain               : Boolean           = this == With.geography.ourMain
  def isOurNatural            : Boolean           = this == With.geography.ourNatural
  def scoutedByUs             : Boolean           = lastFrameScoutedByUs > 0
  def scoutedByEnemy          : Boolean           = lastFrameScoutedByEnemy > 0
  def plannedExpoRecently     : Boolean           = isNeutral && With.framesSince(lastPlannedExpo) < Minutes(1)()
  def resources               : Seq[UnitInfo]     = minerals.view ++ gas

  lazy val overlooks: Vector[(Tile, Double)] = {
    val exit = zone.exitOriginal
    if (exit.isEmpty || naturalOf.isEmpty) Vector.empty else {
      val exitAltitude = exit.get.pixelCenter.altitude
      naturalOf.get.zone.tiles.view
        .filter(_.walkable)
        .filter(_.altitudeUnchecked > exitAltitude)
        .filter(_.groundTiles(exit.get.pixelCenter) < 48)
        .map(tile => (tile, tile.center.pixelDistance(exit.get.pixelCenter)))
        .filter(_._2 < 32 * ?(With.self.isTerran, 11 + 4, 6 + 4))
        .toVector
        .sortBy(_._2)
    }
  }

  private lazy val initialResources = With.units.all.filterNot(_.isBlocker).filter(_.pixelDistanceCenter(townHallTile.topLeftPixel.add(64, 48)) < 32 * 9).toVector
  lazy val harvestingArea                    = new TileRectangle(initialResources.view.flatMap(_.tiles) ++ townHallArea.tiles)
  lazy val harvestingTrafficTiles: Set[Tile] = harvestingArea.tiles.filter(t => t.x > 0 && t.y > 0 && t.x < With.mapTileWidth && t.y < With.mapTileHeight).toSet

  lazy val heart: Tile = {
    val centroid = if (initialResources.isEmpty) townHallArea.center.subtract(Points.middle) else Maff.centroid(initialResources.view.map(_.pixel))
    val direction = centroid.subtract(townHallArea.center)
    if (Math.abs(direction.x) > Math.abs(direction.y))
         if (direction.x < 0) townHallTile.add(-2, 1) else townHallTile.add(5, 1)
    else if (direction.y < 0) townHallTile.add(1, -2) else townHallTile.add(1, 4)
  }

  lazy val gasDirection     : Direction = Maff.centroid(gas.map(_.pixel)).subtract(townHallArea.midPixel).direction
  lazy val mineralDirection : Direction = Maff.centroid(minerals.map(_.pixel)).subtract(townHallArea.midPixel).direction

  lazy val resourcePaths: Map[UnitInfo, Iterable[Tile]] = resources.map(resource => (resource, {
    // Draw a shortest-path line from each resource to the town hall.
    // Where multiple equally-short lines are available, take the one closest to the heart.
    // Count all tiles in that line.
    val townHallTiles = townHallArea.tiles
    def hallDistanceSquared(resourceTile: Tile): Double = townHallTiles.map(resourceTile.tileDistanceSquared).min
    val resourceTiles   = resource.tileArea.tiles
    val bestDistance    = resourceTiles.map(hallDistanceSquared).min
    val from            = resourceTiles.filter(hallDistanceSquared(_) <= bestDistance).minBy(_.tileDistanceSquared(heart))
    val to              = townHallArea.tiles.minBy(_.tileDistanceSquared(from))
    val route           = Shapes.Ray(from.center, to.center).map(_.tile).toVector
    route
  })).toMap

  lazy val resourcePathTiles: Set[Tile] = {
    var output: Set[Tile] = Set.empty
    output ++= resourcePaths.values.flatten
    // Avoid blocking the path where workers are likely to pop out of gas
    // by blocking tiles adjacent to the Nexus that could be along the critical path
    output ++= gas.flatMap(_.tileArea.tilesSurrounding.map(t => townHallArea.expand(1, 1).tiles.minBy(_.tileDistanceSquared(t))))
    // Avoid trapping workers into the mining area by banning tiles which are adjacent to the resource
    output ++= minerals.flatMap(_.tileArea.tilesSurrounding)
    output --= townHallArea.tiles
    output.filter(_.valid)
  }

  def description : String = (
    if      (this == With.geography.ourMain)    "Our main"
    else if (this == With.geography.ourNatural) f"Our ${adjective}natural"
    else(
        (     if (isEnemy)  "Enemy"
        else  if (isOurs)   "Our"
        else  if (isAlly)   "Ally"
        else                "Neutral")
      + " "
      + adjective
      + (     if (isStartLocation     && ! isOurs)  "main"
        else  if (naturalOf.isDefined && ! isOurs)  "natural"
        else                                        "base")))

  override def toString: String = f"$arrow $description $name, ${zone.name} $heart"
}
