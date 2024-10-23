package Macro.Actions

import Lifecycle.With
import Macro.Requests.Get

object PumpGasPumps {
  def apply(quantity: Int = 400): Unit = {
    val pumpType            = With.self.gasClass
    val eligibleBases       = With.geography.ourBases.filter(base => base.townHall.exists(_.remainingCompletionFrames <= pumpType.buildFrames)).sortBy(-_.gasLeft).sortBy(_.townHall.exists(_.complete))
    val eligibleGas         = eligibleBases.flatMap(_.gas)
    val eligibleGasToTake   = eligibleGas.filter(_.player.isNeutral)
    val eligibleBlueprints  = eligibleGasToTake.map(_.tileTopLeft)
    val gasToRequest        = Math.min(quantity, eligibleGas.size)
    With.scheduler.request(this, Get(gasToRequest, pumpType))
  }
}
