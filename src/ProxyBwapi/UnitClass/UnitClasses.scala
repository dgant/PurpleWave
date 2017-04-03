package ProxyBwapi.UnitClass

import Performance.Caching.CacheForever
import bwapi.UnitType

object UnitClasses {
  def all:Iterable[UnitClass] = classByName.get.values
  def None:UnitClass = get(UnitType.None)
  def Unknown:UnitClass = get(UnitType.Unknown)
  
  def get(unitType: UnitType):UnitClass = {
    
    //This implementation is goofy but necessary. Here's why.
    //
    //BWMirror can't compare two UnitTypes.
    //Two UnitTypes are usually, but not *always* the same object.
    //In particular, Unit.getTrainingQueue tends to produce unique UnitType objects.
    //ie. a UnitType.Protoss_Probe may not equal a UnitType.Protoss_Probe that came from the training queue
    //
    //The only unique identifier we have for a UnitType is its toString() value.
    //However, calling that on a UnitType incurs BWMIrror overhead.
    //
    //So, for optimal performance, we use a lookup value of a string when possible,
    //and invoke toString() only as necessary
    
    val typeName = namesByType.get.get(unitType).getOrElse(unitType.toString)
    classByName.get(typeName)
  }
  
  private val namesByType = new CacheForever[Map[UnitType, String]](() =>
    UnitTypes.all.map(unitType => (unitType, unitType.toString)).toMap)
  
  private val classByName = new CacheForever[Map[String, UnitClass]](() =>
    UnitTypes.all.map(unitType => (unitType.toString, new UnitClass(unitType))).toMap)
}
