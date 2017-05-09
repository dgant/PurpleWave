package Planning.Plans.Macro.Automatic.Gas

import Lifecycle.With
import Planning.Plans.Macro.Automatic.Continuous.TrainContinuously
import ProxyBwapi.UnitClass.UnitClass

abstract class BuildGasPumps(pumpType: UnitClass) extends TrainContinuously(pumpType) {
  
  description.set("Builds Pylons just-in-time to prevent supply block")
  
  override def maxDesirable: Int = With.geography.ourBases.flatten(_.gas).count(_.gasLeft > 0)
}
