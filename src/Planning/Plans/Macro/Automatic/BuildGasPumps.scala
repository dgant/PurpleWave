package Planning.Plans.Macro.Automatic

import Lifecycle.With
import ProxyBwapi.UnitClass.UnitClass

abstract class BuildGasPumps(pumpType: UnitClass) extends TrainContinuously(pumpType) {
  
  description.set("Build gas pumps on all our geysers")
  
  override def maxDesirable: Int = With.geography.ourBases.flatten(_.gas).count(_.gasLeft > 0)
}
