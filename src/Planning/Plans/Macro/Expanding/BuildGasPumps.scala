package Planning.Plans.Macro.Expanding

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plan
import ProxyBwapi.UnitClasses.UnitClass

class BuildGasPumps(quantity: Int = Int.MaxValue, pumpType: UnitClass = With.self.gasClass) extends Plan {
  
  override def onUpdate(): Unit = {
    val eligibleBases       = With.geography.ourBases.filter(base => base.townHall.exists(_.remainingCompletionFrames <= pumpType.buildFrames)).toSeq.sortBy(-_.gasLeft).sortBy(_.townHall.exists(_.complete))
    val eligibleGas         = eligibleBases.flatMap(_.gas)
    val eligibleGasToTake   = eligibleGas.filter(_.player.isNeutral)
    val eligibleBlueprints  = eligibleGasToTake.map(_.tileTopLeft)
    val gasToRequest        = Math.min(quantity, eligibleGas.size)
    With.scheduler.request(this, Get(gasToRequest, pumpType))
  }
}
