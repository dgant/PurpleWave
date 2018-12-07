package Information.Geography.Types

import Information.Geography.Pathfinding.ZonePath
import Information.Grids.Movement.GridGroundDistance
import Lifecycle.With
import Mathematics.Formations.Designers.FormationZone
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption
import bwta.Region

import scala.collection.JavaConverters._
import scala.collection.mutable

class Zone(
  val name        : String,
  val bwtaRegion  : Region,
  val boundary    : TileRectangle,
  val tiles       : mutable.Set[Tile]) {

  val tilesSeq = tiles.toSeq
  lazy val  edges             : Array[Edge]         = With.geography.edges.filter(_.zones.contains(this)).toArray
  lazy val  bases             : Array[Base]         = With.geography.bases.filter(_.townHallTile.zone == this).toArray
  lazy val  border            : Set[Tile]           = tiles.filter(_.adjacent8.exists( ! tiles.contains(_))).toSet
  lazy val  perimeter         : Set[Tile]           = tiles.filter(tile => tile.tileDistanceFromEdge <= 1 || ! tile.adjacent8.forall(With.grids.walkableTerrain.get)).toSet
  lazy val  centroid          : Tile                = if (tiles.isEmpty) new Pixel(bwtaRegion.getCenter).tileIncluding else tiles.minBy(_.tileDistanceSquared(new Pixel(bwtaRegion.getCenter).tileIncluding))
  lazy val  area              : Double              = bwtaRegion.getPolygon.getArea
  lazy val  points            : Iterable[Pixel]     = bwtaRegion.getPolygon.getPoints.asScala.map(new Pixel(_)).toVector
  lazy val  island            : Boolean             = With.geography.startBases.count(st => With.paths.groundPathExists(st.heart, centroid)) < 2
  lazy val  tilesBuildable    : Array[Tile]         = { With.grids.buildableTerrain.initialize(); tiles.filter(With.grids.buildableTerrain.get).toArray }
  lazy val  maxMobility       : Int                 = ByOption.max(tiles.map(With.grids.mobilityGround.get)).getOrElse(0)
  lazy val  unwalkable        : Boolean             = ! tiles.exists(With.grids.walkable.get)
  lazy val  formation         : FormationZone       = new FormationZone(this)

  lazy val exitDistanceGrid: GridGroundDistance = new GridGroundDistance(exit.map(_.tiles).getOrElse(Vector(centroid)): _*)
  lazy val exit: Option[Edge] = ByOption.minBy(edges)(edge => With.geography.startLocations.map(_.groundPixels(edge.pixelCenter)).max)

  lazy val distanceGrid: GridGroundDistance = new GridGroundDistance(if (bases.size == 1) bases.head.heart else centroid)

  var units: Vector[UnitInfo]  = Vector.empty
  var unitBuffer: mutable.ArrayBuffer[UnitInfo] = new mutable.ArrayBuffer[UnitInfo]()
  var friendlyGatherers: Set[FriendlyUnitInfo] = Set.empty

  var owner     : PlayerInfo        = With.neutral
  var contested : Boolean           = false
  var walledIn  : Boolean           = false
  
  def contains(tile: Tile)    : Boolean = boundary.contains(tile) && tiles.contains(tile)
  def contains(pixel: Pixel)  : Boolean = contains(pixel.tileIncluding)
  
  def pathTo          (to: Zone): Option[ZonePath]  = With.paths.zonePath(this, to)
  def canWalkTo       (to: Zone): Boolean           = pathTo(to).isDefined
  def distancePixels  (to: Zone): Double            = centroid.groundPixels(to.centroid)
  
  override def toString: String = (
    name
    + " "
    + (if (bases.nonEmpty) "(" + bases.map(_.name).mkString(", ") + ")" else "")
      + " "
    + centroid
  )
}
