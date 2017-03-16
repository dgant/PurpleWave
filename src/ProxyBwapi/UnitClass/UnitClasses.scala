package ProxyBwapi.UnitClass

import Performance.Caching.CacheForever
import bwapi.UnitType

object UnitClasses {
  def get(unitType: UnitType):UnitClass = mapping.get(unitType)
  def all:Iterable[UnitClass] = mapping.get.values
  
  private val mapping = new CacheForever[Map[UnitType, UnitClass]](() => UnitTypes.all.map(unitType => (unitType, new UnitClass(unitType))).toMap)
}
