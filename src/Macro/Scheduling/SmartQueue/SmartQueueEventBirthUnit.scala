package Macro.Scheduling.SmartQueue

import ProxyBwapi.UnitClass.UnitClass

class SmartQueueEventBirthUnit(frame: Int, unitClass: UnitClass) extends SmartQueueEvent(frame) {
  override def apply(queueState: SmartQueueState): Unit = {
    queueState.unitsNow(unitClass) += 1
    queueState.buildersAvailable(unitClass) += 1
    queueState.supplyTotal += unitClass.supplyProvided
  }
}
