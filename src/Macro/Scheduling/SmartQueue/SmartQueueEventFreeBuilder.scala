package Macro.Scheduling.SmartQueue

import ProxyBwapi.UnitClasses.UnitClass

class SmartQueueEventFreeBuilder(frame: Int, unitClass: UnitClass) extends SmartQueueEvent(frame) {
  override def apply(queueState: SmartQueueState): Unit = {
    queueState.buildersAvailable(unitClass) += 1
  }
}
