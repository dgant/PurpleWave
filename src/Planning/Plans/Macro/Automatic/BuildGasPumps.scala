package Planning.Plans.Macro.Automatic

import Lifecycle.With
import ProxyBwapi.UnitClass.UnitClass

abstract class BuildGasPumps(pumpType: UnitClass) extends TrainContinuously(pumpType) {
  
  description.set("Builds gas pumps just-in-time to prevent supply block")
  
  override def maxDesirable: Int = With.geography.ourBases.flatten(_.gas).count(_.gasLeft > 0)
}
