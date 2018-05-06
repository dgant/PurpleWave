package Macro.Scheduling.SmartQueue

import ProxyBwapi.UnitClasses.UnitClass

abstract class SmartQueueEvent(val frame: Int) {
  def apply(queueState: SmartQueueState)
}



