package Information.Geography.Types

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{PixelRay, SpecificPoints, Tile, TileRectangle}
import Performance.Cache
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.{Forever, Minutes}

final class Base(val name: String, val townHallTile: Tile, val tiles: Set[Tile]) {
        val isStartLocation   : Boolean           = With.geography.startLocations.contains(townHallTile)
        val townHallArea      : TileRectangle     = Protoss.Nexus.tileArea.add(townHallTile)
        val radians           : Double            = SpecificPoints.middle.radiansTo(townHallArea.center)
        val zone              : Zone              = With.geography.zoneByTile(townHallTile)
  lazy  val metro             : Metro             = With.geography.metros.find(_.bases.contains(this)).get
  lazy  val economicValue     : Cache[Double]     = new Cache(() => units.view.filter(u => u.unitClass.isBuilding || u.unitClass.isWorker).map(_.subjectiveValue).sum)
  lazy  val plannedExpo       : Cache[Boolean]    = new Cache(() => owner.isNeutral && (
    With.units.ours.exists(u => u.intent.toBuildTile.exists(t => t.base.contains(this) && (! townHallArea.contains(t) || u.intent.toBuild.exists(_.isTownHall))))
    || units.exists(u => u.isOurs && u.unitClass.isBuilding && ! townHallArea.contains(u.tileTopLeft))))
  var natural                 : Option[Base]      = None
  var naturalOf               : Option[Base]      = None
  var townHall                : Option[UnitInfo]  = None
  var units                   : Vector[UnitInfo]  = Vector.empty
  var gas                     : Vector[UnitInfo]  = Vector.empty
  var minerals                : Vector[UnitInfo]  = Vector.empty
  var owner                   : PlayerInfo        = With.neutral
  var enemyCombatValue        : Double            = _
  var workerCount             : Int               = _
  val saturation              : Cache[Double]     = new Cache(() => workerCount.toDouble / (1 + 3 * gas.size + 2 * minerals.size))
  var mineralsLeft            : Int               = 0
  var gasLeft                 : Int               = 0
  var lastPlannedExpo         : Int               = - Forever()
  var lastFrameScoutedByUs    : Int               = 0
  var lastFrameScoutedByEnemy : Int               = 0
  def isOurs                  : Boolean           = owner.isUs
  def isAlly                  : Boolean           = owner.isAlly
  def isEnemy                 : Boolean           = owner.isEnemy
  def isNeutral               : Boolean           = owner.isNeutral
  def isOurMain               : Boolean           = With.geography.ourMain == this
  def isOurNatural            : Boolean           = With.geography.ourNatural == this
  def scoutedByUs             : Boolean           = lastFrameScoutedByUs > 0
  def scoutedByEnemy          : Boolean           = lastFrameScoutedByEnemy > 0
  def plannedExpoRecently     : Boolean           = plannedExpo() || With.framesSince(lastPlannedExpo) < Minutes(1)()
  def resources               : Seq[UnitInfo]     = minerals.view ++ gas
  private lazy val _initialResources = With.units.all.filterNot(_.isBlocker).filter(_.pixelDistanceCenter(townHallTile.topLeftPixel.add(64, 48)) < 32 * 9).toVector
  lazy val harvestingArea = new TileRectangle(_initialResources.view.flatMap(_.tiles) ++ townHallArea.tiles)
  lazy val heart: Tile = {
    val centroid = if (_initialResources.isEmpty) townHallArea.center.subtract(SpecificPoints.middle) else Maff.centroid(_initialResources.view.map(_.pixel))
    val direction = centroid.subtract(townHallArea.center)
    if (Math.abs(direction.x) > Math.abs(direction.y))
         if (direction.x < 0) townHallTile.add(-2, 1) else townHallTile.add(5, 1)
    else if (direction.y < 0) townHallTile.add(1, -2) else townHallTile.add(1, 4)
  }

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
    val route           = PixelRay(from.center, to.center)
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

  override def toString: String = f"$description $name, ${zone.name} $heart"

  def description: String = (
    if (this == With.geography.ourMain) "Our main"
    else if (this == With.geography.ourNatural) "Our natural"
    else
      ((if (isEnemy) "Enemy" else if (isOurs) "Our" else if (isAlly) "Ally" else "Neutral")
      + (if (isStartLocation && ! isOurs) " main" else if (naturalOf.isDefined && ! isOurs) " natural" else " base")))
}
