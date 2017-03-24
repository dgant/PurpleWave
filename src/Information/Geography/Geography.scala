package Information.Geography

import Geometry._
import Information.Geography.Calculations.{ZoneBuilder, ZoneUpdater}
import Information.Geography.Types.{Base, Zone}
import Performance.Caching.{Cache, CacheForever, Limiter}
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import bwapi.TilePosition

class Geography {
  
  def zones               : Iterable[Zone]          = zoneCache.get
  def bases               : Iterable[Base]          = zones.flatten(_.bases)
  def ourBases            : Iterable[Base]          = bases.filter(_.zone.owner == With.self)
  def enemyBases          : Iterable[Base]          = bases.filterNot(base => List(With.self, With.neutral).contains(base.zone.owner))
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
  
  def onFrame() {
    zoneUpdateLimiter.act()
  }
  
  private val zoneCache         = new CacheForever(() => ZoneBuilder.build)
  private val zoneUpdateLimiter = new Limiter(2, () => ZoneUpdater.update())
}
