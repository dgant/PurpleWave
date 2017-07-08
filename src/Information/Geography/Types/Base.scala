package Information.Geography.Types

import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

class Base(val townHallTile: Tile)
{
  lazy val  zone            : Zone              = With.geography.zoneByTile(townHallTile)
  lazy val  townHallArea    : TileRectangle     = Protoss.Nexus.tileArea.add(townHallTile)
  lazy val  isStartLocation : Boolean           = With.geography.startLocations.exists(_ == townHallTile)
  lazy val  isOurMain       : Boolean           = With.geography.ourMain.contains(this)
  var       isNaturalOf     : Option[Base]      = None
  var       townHall        : Option[UnitInfo]  = None
  var       harvestingArea  : TileRectangle     = townHallArea
  var       heart           : Tile              = harvestingArea.midpoint
  var       gas             : Set[UnitInfo]     = Set.empty
  var       minerals        : Set[UnitInfo]     = Set.empty
  var       workers         : Set[UnitInfo]     = Set.empty
  var       walledIn        : Boolean           = false
  var       planningToTake  : Boolean           = false
  var       owner           : PlayerInfo        = With.neutral
  
  var mineralsLeft      = 0
  var gasLeft           = 0
  var lastScoutedFrame  = 0
  
  
  def resources: Set[UnitInfo] = minerals ++ gas
}
