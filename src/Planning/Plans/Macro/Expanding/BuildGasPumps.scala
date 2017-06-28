package Planning.Plans.Macro.Expanding

import Lifecycle.With
import Planning.Plans.Macro.Automatic.TrainContinuously
import ProxyBwapi.UnitClass.UnitClass

abstract class BuildGasPumps(pumpType: UnitClass) extends TrainContinuously(pumpType) {
  
  description.set("Build gas pumps on all our geysers")
  
  override def maxDesirable: Int = With.geography.ourBases
    .filter(_.townHall.exists(townHall =>
      townHall.complete ||
      townHall.remainingBuildFrames < pumpType.buildFrames))
    .flatten(_.gas)
    .count(_.gasLeft > 100)
}
