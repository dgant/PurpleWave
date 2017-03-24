package Information.Geography

import Information.Geography.Types.{Base, Zone}
import Performance.Caching.{CacheForever, Limiter}
import Startup.With

class Bases {
  
  def zones:Iterable[Zone] = {
    zoneUpdateLimiter.act()
    zoneCache.get
  }
  
  private val zoneCache = new CacheForever[Iterable[Zone]](() => BuildZones.build)
  private val zoneUpdateLimiter = new Limiter(2, updateZones)
  
  private def updateZones() = {
    zoneCache.get.foreach(updateZone)
  }
  
  private def updateZone(zone:Zone) {
    zone.bases.foreach(updateBase)
  }
  
  private def updateBase(base:Base) {
    updateTownHall(base)
  }
  
  private def updateTownHall(base: Base) {
    val townHall = With.units.buildings
      .filter(_.unitClass.isTownHall)
      .filter(townHall => base.zone.contains(townHall.pixelCenter))
      .toList
      .sortBy(_.tileDistance(base.townHallArea.midpoint))
      .headOption
    base.townHall = townHall
    base.zone.owner = townHall.map(_.player).getOrElse(With.game.neutral)
  }
  
  private def updateResources(base: Base) {
    base.minerals = base.mineralUnits.filter(_.alive).map(_.mineralsLeft).sum
    base.gas      = With.units.all
      .filter(unit => unit.unitClass.isGas && base.zone.contains(unit.pixelCenter))
      .map(_.gasLeft)
      .sum
  }
  
}
