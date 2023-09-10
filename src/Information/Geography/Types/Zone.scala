package Information.Geography.Types

import Information.Geography.Calculations.UpdateZones
import Information.Grids.Movement.GridGroundDistance
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Direction, Pixel, Tile}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?
import bwta.Region

final class Zone(val name: String, val bwemRegion: Region, val tiles: Set[Tile]) extends Geo {
  lazy  val zones             : Vector[Zone]        = Vector(this)
  lazy  val bases             : Vector[Base]        = With.geography.bases.filter(_.townHallTile.zone == this)
  lazy  val edges             : Vector[Edge]        = With.geography.edges.filter(_.zones.contains(this))
  lazy  val metro             : Option[Metro]       = With.geography.metros.find(_.zones.contains(this))
  lazy  val entranceOriginal  : Option[Edge]        = UpdateZones.calculateEntrance(this)
  lazy  val exitOriginal      : Option[Edge]        = Maff.minBy(edges)(e => With.geography.startBases.map(_.heart).map(e.distanceGrid.get).max)
  lazy  val distanceGrid      : GridGroundDistance  = new GridGroundDistance(if(bases.length == 1) bases.head.heart else centroid)
  lazy  val exitDirection     : Option[Direction]   = exitOriginal.map(_.pixelCenter.subtract(heart.center).direction)
  lazy  val isStartLocation   : Boolean             = bases.exists(_.isStartLocation)
  var contested               : Boolean             = false
  var walledIn                : Boolean             = false
  var exitNow                 : Option[Edge]        = None
  var entranceNow             : Option[Edge]        = None

  var _units: Vector[UnitInfo] = _
  def setUnits(argUnits: Vector[UnitInfo]): Unit = _units = argUnits
  def units: Vector[UnitInfo] = _units

  private var _owner: PlayerInfo = ?(tiles.contains(new Tile(With.game.self.getStartLocation)), With.self, With.neutral)
  def setOwner(argOwner: PlayerInfo): Unit = _owner = argOwner
  def owner: PlayerInfo = _owner

  def heart           : Tile = bases.sortBy(_.mineralsLeft).sortBy(_.owner.isNeutral).headOption.map(_.heart).getOrElse(centroid)
  def downtown        : Tile = heart.center.midpoint(exitOriginal.map(_.pixelCenter).getOrElse(heart.center)).walkableTile
  def exitNowOrHeart  : Tile = exitNow.map(_.pixelCenter.walkableTile).getOrElse(heart)

  def edgeTo(to: Pixel) : Option[Edge] = Maff.minBy(edges)(_.pixelCenter.groundPixels(to))

  override def toString: String = f"$name ${?(bases.nonEmpty, f"(${bases.map(b => f"${b.name} - ${b.description}").mkString(", ")}) ", "")}$centroid"

  /////////////////////
  // Wall generation //
  /////////////////////

  lazy val wallPerimeter                  : Vector[Tile]  = tiles.flatMap(_.adjacent9).view.filter(tile => ! tile.walkable && tile.adjacent4.exists(_.walkable) && ! wallPerimeterHall.contains(tile) && ! wallPerimeterGas.contains(tile)).toVector.sortBy(t => exitOriginal.map(_.pixelTowards(this)).getOrElse(centroid.center).radiansTo(t.center))
  lazy val wallPerimeterSplitStartA       : Tile          = exitOriginal.map(e => wallPerimeter.minBy(_.center.pixelDistanceSquared(e.sidePixels.head))).getOrElse(wallPerimeter.head)
  lazy val wallPerimeterSplitStartB       : Tile          = exitOriginal.map(e => wallPerimeter.minBy(_.center.pixelDistanceSquared(e.sidePixels.last))).getOrElse(wallPerimeter.last)
  lazy val wallPerimeterSplitLongA        : Vector[Tile]  = wallPerimeter.filter(t => t.tileDistanceSquared(wallPerimeterSplitStartA) < t.tileDistanceSquared(wallPerimeterSplitStartB)).sortBy(_.tileDistanceSquared(wallPerimeterSplitStartA))
  lazy val wallPerimeterSplitLongB        : Vector[Tile]  = wallPerimeter.filter(t => t.tileDistanceSquared(wallPerimeterSplitStartB) < t.tileDistanceSquared(wallPerimeterSplitStartA)).sortBy(_.tileDistanceSquared(wallPerimeterSplitStartB))
  lazy val wallPerimeterA                 : Vector[Tile]  = wallPerimeterSplitLongA.take(Math.min(25, wallPerimeter.length / 2))
  lazy val wallPerimeterB                 : Vector[Tile]  = wallPerimeterSplitLongB.take(Math.min(25, wallPerimeter.length / 2))
  lazy val wallPerimeterACloserToEntrance : Boolean       = wallPerimeterA == Seq(wallPerimeterA, wallPerimeterB).minBy(wp => Maff.min(wp.view.map(_.pixelDistance(entranceOriginal.map(_.pixelCenter).getOrElse(centroid.center)))).getOrElse(0d))
  lazy val wallPerimeterEntrance          : Vector[Tile]  = if (wallPerimeterACloserToEntrance) wallPerimeterA else wallPerimeterB
  lazy val wallPerimeterExit              : Vector[Tile]  = if (wallPerimeterACloserToEntrance) wallPerimeterB else wallPerimeterA
  lazy val wallPerimeterHall              : Vector[Tile]  = bases.flatMap(_.townHallArea.tilesAtEdge)
  lazy val wallPerimeterGas               : Vector[Tile]  = bases.flatMap(_.gas.flatMap(_.tileArea.tilesAtEdge))
}
