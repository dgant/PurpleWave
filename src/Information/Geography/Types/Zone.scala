package Information.Geography.Types

import Information.Grids.Movement.{GridFlowField, GridGroundDistance}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.?
import bwta.Region

import scala.collection.mutable

final class Zone(val name: String, val bwemRegion: Region, val tiles: Set[Tile]) extends Geo {
        val zones             : Vector[Zone]              = Vector(this)
        val units             : UnorderedBuffer[UnitInfo] = new UnorderedBuffer
  lazy  val bases             : Vector[Base]              = With.geography.bases.filter(_.townHallTile.zone == this)
  lazy  val gridOrigin        : Tile                      = ?(bases.length == 1, bases.head.heart, centroid)
  lazy  val metro             : Option[Metro]             = With.geography.metros.find(_.zones.contains(this))
  lazy  val distanceGrid      : GridGroundDistance        = new GridGroundDistance(gridOrigin)
  lazy  val flowField         : GridFlowField             = new GridFlowField(gridOrigin)
  var contested               : Boolean                   = false
  var walledIn                : Boolean                   = false

  private var _owner: PlayerInfo = ?(tiles.contains(new Tile(With.game.self.getStartLocation)), With.self, With.neutral)
  def setOwner(argOwner: PlayerInfo): Unit = _owner = argOwner
  def owner: PlayerInfo = _owner

  def heart           : Tile = bases.sortBy(_.mineralsLeft).sortBy(_.owner.isNeutral).headOption.map(_.heart).getOrElse(centroid)
  def downtown        : Tile = heart.center.midpoint(exitOriginal.map(_.pixelCenter).getOrElse(heart.center)).walkableTile
  def exitNowOrHeart  : Tile = exitNow.map(_.pixelCenter.walkableTile).getOrElse(heart)

  override def toString: String = f"$name ${if (bases.nonEmpty) f"(${bases.map(b => f"${b.name} - ${b.description}").mkString(", ")}) " else ""}$centroid"

  /////////////////////
  // Wall generation //
  /////////////////////

  lazy val wallPerimeter                  : Vector[Tile]  = tiles.flatMap(_.adjacent9).view.filter(tile => ! tile.walkable && tile.adjacent4.exists(_.walkable) && ! wallPerimeterHall.contains(tile) && ! wallPerimeterGas.contains(tile)).toVector
  lazy val wallPerimeterSplitStartA       : Tile          = exitOriginal.map(e => wallPerimeter.minBy(_.center.pixelDistanceSquared(e.sidePixels.head))).getOrElse(wallPerimeter.head)
  lazy val wallPerimeterSplitStartB       : Tile          = exitOriginal.map(e => wallPerimeter.minBy(_.center.pixelDistanceSquared(e.sidePixels.last))).getOrElse(wallPerimeter.last)
  private def wallPerimeterDistances(start: Tile): Map[Tile, Int] = {
    val perimeter = wallPerimeter.toSet
    val output    = new mutable.HashMap[Tile, Int]
    val horizon   = mutable.Queue(start)
    output(start) = 0
    while (horizon.nonEmpty) {
      val current = horizon.dequeue()
      current.adjacent8.foreach(next => if (perimeter.contains(next) && ! output.contains(next)) {
        output(next) = output(current) + 1
        horizon.enqueue(next)
      })
    }
    output.toMap
  }
  private lazy val wallPerimeterDistancesA = wallPerimeterDistances(wallPerimeterSplitStartA)
  private lazy val wallPerimeterDistancesB = wallPerimeterDistances(wallPerimeterSplitStartB)
  private def wallPerimeterBelongsToA(tile: Tile): Boolean = (wallPerimeterDistancesA.get(tile), wallPerimeterDistancesB.get(tile)) match {
    case (Some(a), Some(b)) => a < b || (a == b && tile.tileDistanceSquared(wallPerimeterSplitStartA) < tile.tileDistanceSquared(wallPerimeterSplitStartB))
    case (Some(_), None)    => true
    case _                  => false
  }
  lazy val wallPerimeterSplitLongA        : Vector[Tile]  = wallPerimeter.filter(wallPerimeterBelongsToA).sortBy(t => (wallPerimeterDistancesA(t), t.y, t.x))
  lazy val wallPerimeterSplitLongB        : Vector[Tile]  = wallPerimeter.filter(t => wallPerimeterDistancesB.contains(t) && ! wallPerimeterBelongsToA(t)).sortBy(t => (wallPerimeterDistancesB(t), t.y, t.x))
  lazy val wallPerimeterA                 : Vector[Tile]  = wallPerimeterSplitLongA.take(25)
  lazy val wallPerimeterB                 : Vector[Tile]  = wallPerimeterSplitLongB.take(25)
  lazy val wallPerimeterACloserToEntrance : Boolean       = wallPerimeterA == Seq(wallPerimeterA, wallPerimeterB).minBy(wp => Maff.min(wp.view.map(_.pixelDistance(entranceOriginal.map(_.pixelCenter).getOrElse(centroid.center)))).getOrElse(0d))
  lazy val wallPerimeterEntrance          : Vector[Tile]  = if (wallPerimeterACloserToEntrance) wallPerimeterA else wallPerimeterB
  lazy val wallPerimeterExit              : Vector[Tile]  = if (wallPerimeterACloserToEntrance) wallPerimeterB else wallPerimeterA
  lazy val wallPerimeterHall              : Vector[Tile]  = bases.flatMap(_.townHallArea.tilesAtEdge)
  lazy val wallPerimeterGas               : Vector[Tile]  = bases.flatMap(_.gas.flatMap(_.tileArea.tilesAtEdge))
}
