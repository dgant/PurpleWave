package Information.Geography

import Geometry._
import Information.Geography.Types.{Base, Zone}
import Performance.Caching.Cache
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import bwapi.TilePosition

class Geography {
  
  private val baseCalculator = new ZoneUpdater
  def zones               : Iterable[Zone]          = ZoneBuilder.build
  def bases               : Iterable[Base]          = zones.flatten(_.bases)
  def ourBases            : Iterable[Base]          = bases.filter(_.zone.owner == With.self)
  def ourTownHalls        : Iterable[UnitInfo]      = ourBases.flatMap(_.townHall)
  def ourHarvestingAreas  : Iterable[TileRectangle] = ourBases.map(_.harvestingArea)
  
  def home:TilePosition = homeCache.get
  private val homeCache = new Cache(5, () =>
    ourBases
      .toList
      .sortBy( ! _.isStartLocation)
      .headOption
      .map(_.townHallRectangle.startInclusive)
      .getOrElse(Positions.tileMiddle))
}
