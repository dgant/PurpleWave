package Information.Geography.Types

import Lifecycle.With
import Mathematics.Points.{PixelRay, SpecificPoints, Tile, TileRectangle}
import Mathematics.Maff
import Performance.Cache
import Planning.UnitMatchers.{MatchBuilding, MatchWorker}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.{Forever, Minutes}

import scala.collection.mutable

class Base(val townHallTile: Tile)
{
  val townHallArea          : TileRectangle     = Protoss.Nexus.tileArea.add(townHallTile)
  lazy val zone             : Zone              = With.geography.zoneByTile(townHallTile)
  lazy val metro            : Metro             = With.geography.metros.find(_.bases.contains(this)).get
  lazy val isStartLocation  : Boolean           = With.geography.startLocations.contains(townHallTile)
  lazy val isOurMain        : Boolean           = With.geography.ourMain == this
  lazy val tiles            : Set[Tile]         = zone.tiles.view.filter(t => t.tileDistanceSlow(heart) < 50 && ! zone.bases.view.filter(_.heart != heart).exists(_.heart.pixelDistanceGround(t) < heart.pixelDistanceGround(t))).toSet
  lazy val economicValue    : Cache[Double]     = new Cache(() => units.view.filter(_.isAny(MatchBuilding, MatchWorker)).map(_.subjectiveValue).sum)
  lazy val plannedExpo      : Cache[Boolean]    = new Cache(() => owner.isNeutral && (
    With.units.ours.exists(u => u.intent.toBuildTile.exists(t => t.base.contains(this) && (! townHallArea.contains(t) || u.intent.toBuild.exists(_.isTownHall))))
    || units.exists(u => u.isOurs && u.unitClass.isBuilding && ! townHallArea.contains(u.tileTopLeft))))
  lazy val radians          : Double            = SpecificPoints.middle.radiansTo(townHallArea.center)
  var isNaturalOf           : Option[Base]      = None
  var townHall              : Option[UnitInfo]  = None
  var units                 : Vector[UnitInfo]  = Vector.empty
  var gas                   : Vector[UnitInfo]  = Vector.empty
  var minerals              : Vector[UnitInfo]  = Vector.empty
  var owner                 : PlayerInfo        = With.neutral
  var lastOwnerChangeFrame  : Int = 0
  var name                  : String            = "Nowhere"
  var defenseValue          : Double            = _
  var workerCount           : Int               = _
  val saturation            : Cache[Double]     = new Cache(() => workerCount.toDouble / (1 + 3 * gas.size + 2 * minerals.size))

  private val _initialResources = With.units.all.filter(u => u.mineralsLeft > With.configuration.blockerMineralThreshold || u.gasLeft > 0).filter(_.pixelDistanceCenter(townHallTile.topLeftPixel.add(64, 48)) < 32 * 9).toVector
  val harvestingArea = new TileRectangle(_initialResources.view.flatMap(_.tiles) ++ townHallArea.tiles)
  val heart: Tile = {
    val centroid = if (_initialResources.isEmpty) townHallArea.center.subtract(SpecificPoints.middle) else Maff.centroid(_initialResources.view.map(_.pixel))
    val direction = centroid.subtract(townHallArea.center)
    val xDominant = Math.abs(direction.x) > Math.abs(direction.y)
    if (xDominant)
          if (direction.x < 0) townHallTile.add(-2, 1) else townHallTile.add(5, 1)
    else if (direction.y < 0) townHallTile.add(1, -2) else townHallTile.add(1, 4)
  }

  private def resourcePathTiles(resource: UnitInfo): Iterable[Tile] = {
    // Draw a shortest-path line from each resource to the town hall.
    // Where multiple equally-short lines are available, take the one closest to the heart.
    // Count all tiles in that line.
    val townHallTiles = townHallArea.tiles
    def hallDistanceSquared(resourceTile: Tile): Double = townHallTiles.map(resourceTile.tileDistanceSquared).min
    val resourceTiles   = resource.tileArea.tiles
    val bestDistance    = resourceTiles.map(hallDistanceSquared).min
    val from            = resourceTiles.filter(hallDistanceSquared(_) <= bestDistance).minBy(_.tileDistanceSquared(heart))
    val to              = townHallArea.tiles.minBy(_.tileDistanceSquared(from))
    val route           = PixelRay(from.center, to.center)
    route
  }
  lazy val resourcePaths: Map[UnitInfo, Iterable[Tile]] = {
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
  var lastPlannedExpo           = - Forever()
  var lastScoutedFrame          = 0
  var lastScoutedByEnemyFrame   = 0

  def plannedExpoRecently = plannedExpo() || With.framesSince(lastPlannedExpo) < Minutes(1)()
  def scouted: Boolean = lastScoutedFrame > 0
  def scoutedByEnemy: Boolean = lastScoutedFrame > 0
  def resources: Vector[UnitInfo] = minerals ++ gas
  def natural: Option[Base] = With.geography.bases.find(_.isNaturalOf.contains(this))
  
  override def toString: String = f"$description $name, ${zone.name} $heart"

  def description: String = (
    if (this == With.geography.ourMain) "Our main"
    else if (this == With.geography.ourNatural) "Our natural"
    else (
      (if (owner.isEnemy) "Enemy" else if (owner.isUs) "Our" else if (owner.isAlly) "Ally" else "Neutral")
      + " "
      + (if (isStartLocation && ! owner.isUs) "main" else if (isNaturalOf.isDefined && ! owner.isUs) "natural" else "base")))
}
