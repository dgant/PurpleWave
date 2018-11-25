package ProxyBwapi.UnitTracking

import ProxyBwapi.UnitInfo.{HistoricalUnitInfo, UnitInfo}

import scala.collection.mutable

class HistoricalUnitTracker {

  private val units = new mutable.HashMap[Int, HistoricalUnitInfo]

  def all: Iterable[HistoricalUnitInfo] = units.values

  def remove(unit: UnitInfo): Unit = {
    units.remove(unit.id)
  }
  def add(unit: UnitInfo): Unit = {
    if ( ! units.contains(unit.id)) {
      units(unit.id) = new HistoricalUnitInfo(unit)
    }
  }
}
