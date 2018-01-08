package Macro.Scheduling.SmartQueue

import ProxyBwapi.UnitClass.UnitClass

abstract class SmartQueueEvent(val frame: Int) {
  def apply(queueState: SmartQueueState)
}



