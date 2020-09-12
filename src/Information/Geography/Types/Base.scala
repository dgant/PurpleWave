package Information.Geography.Types

import Lifecycle.With
import Mathematics.Points.{PixelRay, Tile, TileRectangle}
import Mathematics.PurpleMath
import Performance.Cache
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel._

import scala.collection.mutable

class Base(val townHallTile: Tile)
{
  lazy val  zone            : Zone              = With.geography.zoneByTile(townHallTile)
  lazy val  townHallArea    : TileRectangle     = Protoss.Nexus.tileArea.add(townHallTile)
  lazy val  isStartLocation : Boolean           = With.geography.startLocations.contains(townHallTile)
  lazy val  isOurMain       : Boolean           = With.geography.ourMain == this
  var       isNaturalOf     : Option[Base]      = None
  var       townHall        : Option[UnitInfo]  = None
  var       units           : Vector[UnitInfo]  = Vector.empty
  var       gas             : Vector[UnitInfo]  = Vector.empty
  var       minerals        : Vector[UnitInfo]  = Vector.empty
  var       owner           : PlayerInfo        = With.neutral
  var       name            : String            = "Nowhere"
  var       defenseValue    : Double            = _
  var       workerCount     : Int               = _
  val       saturation      : Cache[Double]     = new Cache(() => workerCount.toDouble / (1 + 3 * gas.size + 2 * minerals.size))

  private var calculatedHarvestingArea: Option[TileRectangle] = None
  private var calculatedHeart: Option[Tile] = None
  def harvestingArea: TileRectangle = {
    if (calculatedHarvestingArea.isDefined) calculatedHarvestingArea.get else {
      // This is called during initialization! So variables like heart aren't populated yet
      val centroid = PurpleMath.centroidTiles(minerals.map(_.tileTopLeft))
      val townHall = townHallTile.add(2, 1)
      val dx = centroid.x - townHall.x
      val dy = centroid.y - townHall.y
      val dxBigger = Math.abs(dx) > Math.abs(dy)
      val boxInitial = (Vector(townHallArea) ++ (minerals.filter(_.mineralsLeft > With.configuration.blockerMineralThreshold) ++ gas)
        .map(_.tileArea))
        .boundary
      val output = TileRectangle(
        boxInitial
          .startInclusive
          .add(
            if (   dxBigger) PurpleMath.clamp(dx, -1, 0) else 0,
            if ( ! dxBigger) PurpleMath.clamp(dy, -1, 0) else 0)
          .clip,
      boxInitial
        .endExclusive
        .add(
          if (   dxBigger) PurpleMath.clamp(dx, 0, 1) else 0,
          if ( ! dxBigger) PurpleMath.clamp(dy, 0, 1) else 0)
        .clip)
      if (minerals.nonEmpty || gas.nonEmpty) {
        calculatedHarvestingArea = Some(output)
      }
      output
    }
  }
  def heart: Tile = {
    if (calculatedHeart.isDefined) calculatedHeart.get else {
      val output = harvestingArea.midpoint
      if (calculatedHarvestingArea.isDefined) {
        calculatedHeart = Some(output)
      }
      output
    }
  }
  private def resourcePathTiles(resource: UnitInfo): Seq[Tile] = {
    // Draw a shortest-path line from each resource to the town hall.
    // Where multiple equally-short lines are available, take the one closest to the heart.
    // Count all tiles in that line.
    val townHallTiles = townHallArea.tiles
    def hallDistanceSquared(resourceTile: Tile): Double = townHallTiles.map(resourceTile.tileDistanceSquared).min
    val resourceTiles   = resource.tileArea.tiles
    val bestDistance    = resourceTiles.map(hallDistanceSquared).min
    val from            = resourceTiles.filter(hallDistanceSquared(_) <= bestDistance).minBy(_.tileDistanceSquared(heart))
    val to              = townHallArea.tiles.minBy(_.tileDistanceSquared(from))
    val route           = PixelRay(from.pixelCenter, to.pixelCenter)
    route.tilesIntersected
  }
  lazy val resourcePaths: Map[UnitInfo, Seq[Tile]] = {
    resources.map(resource => (resource, resourcePathTiles(resource))).toMap
  }
  lazy val resourcePathTiles: Set[Tile] = {
    val output = new mutable.ArrayBuffer[Tile]

    output ++= resourcePaths.values.flatten

    // Avoid blocking the path where workers are likely to pop out of gas
    // by blocking tiles adjacent to the Nexus that could be along the critical path
    val townHallAdjacentTiles = townHallArea.expand(1, 1).tiles
    gas.foreach(_.tileArea.tilesSurrounding.foreach(gasTile => {
      output += townHallAdjacentTiles.minBy(_.tileDistanceSquared(gasTile))
    }))

    // Avoid trapping workers into the mining area by banning tiles which are adjacent to the resource
    output ++= minerals.flatMap(_.tileArea.tilesSurrounding)
    output --= townHallArea.tiles

    output.filter(_.valid).toSet
  }
  
  var mineralsLeft              = 0
  var gasLeft                   = 0
  var lastScoutedFrame          = 0
  var lastScoutedByEnemyFrame   = 0
  
  def scouted: Boolean = lastScoutedFrame > 0
  def scoutedByEnemy: Boolean = lastScoutedFrame > 0
  def resources: Vector[UnitInfo] = minerals ++ gas
  def natural: Option[Base] = With.geography.bases.find(_.isNaturalOf.contains(this))
  
  override def toString: String = name + ", " + zone.name + " " + heart
}
