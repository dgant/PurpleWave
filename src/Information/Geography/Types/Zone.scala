package Information.Geography.Types

import Information.Geography.Calculations.UpdateZones
import Information.Grids.Movement.GridGroundDistance
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo
import bwta.Region

final class Zone(val name: String, val bwemRegion: Region, val tiles: Set[Tile]) {
  lazy val boundary         : TileRectangle     = new TileRectangle(Maff.min(tiles.view.map(_.x)).getOrElse(0), Maff.min(tiles.view.map(_.y)).getOrElse(0), Maff.max(tiles.view.map(_.x)).getOrElse(0), Maff.max(tiles.view.map(_.y)).getOrElse(0))
  lazy val centroid         : Tile              = (if (tiles.isEmpty) new Pixel(bwemRegion.getCenter).tile else tiles.minBy(_.tileDistanceSquared(new Pixel(bwemRegion.getCenter).tile))).center.walkableTile
  lazy val border           : Set[Tile]         = tiles.filter( ! _.adjacent8.forall(tiles.contains)).toSet
  lazy val perimeter        : Set[Tile]         = tiles.filter(tile => tile.tileDistanceFromEdge <= 1 || ! tile.adjacent8.forall(_.walkable)).toSet
  lazy val island           : Boolean           = With.geography.startBases.map(_.heart).count(With.paths.groundPathExists(_, centroid)) < 2
  lazy val edges            : Vector[Edge]      = With.geography.edges.filter(_.zones.contains(this))
  lazy val bases            : Vector[Base]      = With.geography.bases.filter(_.townHallTile.zone == this)
  lazy val metro            : Option[Metro]     = With.geography.metros.find(_.zones.contains(this))
  lazy val exitOriginal     : Option[Edge]      = UpdateZones.calculateExit(this)
  lazy val distanceGrid     : GridGroundDistance = new GridGroundDistance(if (bases.length == 1) bases.head.heart else centroid)
  lazy val exitDistanceGrid : GridGroundDistance = exitOriginal.map(_.distanceGrid).getOrElse(distanceGrid)
  
  var units           : Vector[UnitInfo]  = Vector.empty
  var owner           : PlayerInfo        = if (tiles.contains(new Tile(With.game.self.getStartLocation))) With.self else With.neutral
  var contested       : Boolean           = false
  var walledIn        : Boolean           = false
  var exitNow         : Option[Edge]      = None
  var lastPathfindId  : Long              = Long.MinValue

  def isOurs    : Boolean = owner.isUs
  def isAlly    : Boolean = owner.isAlly
  def isEnemy   : Boolean = owner.isEnemy
  def isNeutral : Boolean = owner.isNeutral
  def heart     : Tile    = bases.sortBy(_.mineralsLeft).sortBy(_.owner.isNeutral).headOption.map(_.heart).getOrElse(centroid)

  override def toString: String = f"$name ${if (bases.nonEmpty) f"(${bases.map(b => f"${b.name} - ${b.description}").mkString(", ")}) " else ""}$centroid"

}
