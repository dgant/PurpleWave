package Information.Geography.Types

import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.PurpleMath
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel._

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
