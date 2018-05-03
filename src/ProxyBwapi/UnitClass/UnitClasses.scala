package ProxyBwapi.UnitClass

import Lifecycle.With
import bwapi.UnitType

object UnitClasses {
  def all: Iterable[UnitClass] = With.proxy.unitClassByTypeName.values
  def None: UnitClass = get(UnitType.None)
  def Unknown: UnitClass = get(UnitType.Unknown)
  
  def get(unitType: UnitType): UnitClass = {
    
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
    
    val typeName = With.proxy.namesByUnitType .getOrElse(unitType, unitType.toString)
    With.proxy.unitClassByTypeName(typeName)
  }
}
