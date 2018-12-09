package Information.Geography.Types

import Lifecycle.With
import Mathematics.Formations.Designers.FormationBase
import Mathematics.Points.{Tile, TileRectangle}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel._

class Base(val townHallTile: Tile)
{
  lazy val  zone            : Zone              = With.geography.zoneByTile(townHallTile)
  lazy val  townHallArea    : TileRectangle     = Protoss.Nexus.tileArea.add(townHallTile)
  lazy val  isStartLocation : Boolean           = With.geography.startLocations.exists(_ == townHallTile)
  lazy val  isOurMain       : Boolean           = With.geography.ourMain == this
  lazy val  formations      : FormationBase     = new FormationBase(this)
  lazy val  harvestingArea  : TileRectangle     = (Vector(townHallArea) ++ (minerals.filter(_.mineralsLeft > With.configuration.blockerMineralThreshold) ++ gas).map(_.tileArea)).boundary
  lazy val  heart           : Tile              = harvestingArea.midpoint
  var       isNaturalOf     : Option[Base]      = None
  var       townHall        : Option[UnitInfo]  = None
  var       units           : Vector[UnitInfo]  = Vector.empty
  var       gas             : Vector[UnitInfo]  = Vector.empty
  var       minerals        : Vector[UnitInfo]  = Vector.empty
  var       owner           : PlayerInfo        = With.neutral
  var       name            : String            = "Nowhere"
  var       defenseValue    : Double            = _
  var       workerCount     : Int               = _
  
  var mineralsLeft      = 0
  var gasLeft           = 0
  var lastScoutedFrame  = 0
  
  def scouted: Boolean = lastScoutedFrame > 0
  def resources: Vector[UnitInfo] = minerals ++ gas
  def natural: Option[Base] = With.geography.bases.find(_.isNaturalOf.contains(this))
  
  override def toString: String = name + ", " + zone.name + " " + heart
}
