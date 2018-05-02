package Planning.Plans.Macro.Automatic

import Lifecycle.With
import ProxyBwapi.UnitClass.UnitClass

class AddAddons(unitClass: UnitClass, maximum: Int = Int.MaxValue) extends TrainContinuously(unitClass, maximum) {
  
  description.set("Continuously add on " + unitClass)
  
  override protected def buildCapacity: Int = {
    With.units.countOurs(b => b.is(unitClass.whatBuilds._1) && b.addon.isEmpty)
  }
}
