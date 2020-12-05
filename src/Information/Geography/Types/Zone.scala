package Information.Geography.Types

import Information.Geography.Pathfinding.Types.ZonePath
import Information.Grids.Lambda.GridFixedLambdaBoolean
import Information.Grids.Movement.GridGroundDistance
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption
import bwta.Region

import scala.collection.mutable

class Zone(
  val name        : String,
  val bwtaRegion  : Region,
  val boundary    : TileRectangle,
  val tiles       : mutable.Set[Tile]) {

  val tilesSeq: Seq[Tile] = tiles.toSeq
  val tileGrid = new GridFixedLambdaBoolean(i => tiles.contains(new Tile(i)))
  lazy val  edges             : Vector[Edge]        = With.geography.edges.filter(_.zones.contains(this))
  lazy val  adjacentZones     : Vector[Zone]        = edges.map(_.otherSideof(this))
  lazy val  bases             : Vector[Base]        = With.geography.bases.filter(_.townHallTile.zone == this)
  lazy val  border            : Set[Tile]           = tiles.filter(_.adjacent8.exists( ! tiles.contains(_))).toSet
  lazy val  perimeter         : Set[Tile]           = tiles.filter(tile => tile.tileDistanceFromEdge <= 1 || ! tile.adjacent8.forall(With.grids.walkableTerrain.get)).toSet
  lazy val  centroid          : Tile                = (if (tiles.isEmpty) new Pixel(bwtaRegion.getCenter).tileIncluding else tiles.minBy(_.tileDistanceSquared(new Pixel(bwtaRegion.getCenter).tileIncluding))).pixelCenter.nearestWalkableTile
  lazy val  area              : Double              = tiles.size // Previously taken from BWTA region area
  lazy val  island            : Boolean             = With.geography.startBases.count(st => With.paths.groundPathExists(st.heart, centroid)) < 2
  lazy val  tilesBuildable    : Vector[Tile]         = { With.grids.buildableTerrain.initialize(); tiles.view.filter(With.grids.buildableTerrain.get).toVector }
  lazy val  unwalkable        : Boolean             = ! tiles.exists(With.grids.walkable.get)

  lazy val exitDistanceGrid: GridGroundDistance = exit.map(_.distanceGrid).getOrElse(new GridGroundDistance(centroid))
  lazy val exit: Option[Edge] = calculateExit
  lazy val distanceGrid: GridGroundDistance = new GridGroundDistance(if (bases.length == 1) bases.head.heart else centroid)
  
  var units: Vector[UnitInfo]  = Vector.empty
  var unitBuffer: mutable.ArrayBuffer[UnitInfo] = new mutable.ArrayBuffer[UnitInfo]()
  var owner     : PlayerInfo        = With.neutral
  var contested : Boolean           = false
  var walledIn  : Boolean           = false
  var exitNow   : Option[Edge]      = None

  def calculateExit: Option[Edge] = {
    ByOption.minBy(edges)(edge =>
      ByOption.min(With.geography.enemyBases.map(enemyBase => edge.distanceGrid.get(enemyBase.heart)))
        .getOrElse(edge.distanceGrid.get(With.scouting.threatOrigin)))
  }
  
  def contains(tile: Tile)    : Boolean = boundary.contains(tile) && tiles.contains(tile)
  def contains(pixel: Pixel)  : Boolean = contains(pixel.tileIncluding)
  
  def pathTo          (to: Zone): Option[ZonePath]  = With.paths.zonePath(this, to)
  def canWalkTo       (to: Zone): Boolean           = pathTo(to).isDefined
  def distancePixels  (to: Zone): Double            = centroid.groundPixels(to.centroid)
  def heart: Tile = bases.sortBy(_.mineralsLeft).sortBy(_.owner.isNeutral).headOption.map(_.heart).getOrElse(centroid)

  // Cached information for pathfinding
  var lastPathfindId: Long = Long.MinValue

  // Cache the hashCode
  final override val hashCode: Int = super.hashCode

  final override def toString: String = (
    name
    + (if (bases.nonEmpty) " (" + bases.map(b => b.name + " - " + b.description).mkString(", ") + ")" else "")
    + " "
    + centroid
  )
}
