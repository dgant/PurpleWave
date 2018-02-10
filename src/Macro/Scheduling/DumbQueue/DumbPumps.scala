package Macro.Scheduling.DumbQueue

import ProxyBwapi.UnitClass.UnitClass
import Utilities.CountMap

class DumbPumps {
  
  private val unitCount: CountMap[UnitClass] = new CountMap[UnitClass]
  
  def reset() {
    unitCount.clear()
  }
  
  def consume(unitClass: UnitClass, count: Int) {
    unitCount(unitClass) += count
  }
  
  def consumed(unitClass: UnitClass): Int = {
    unitCount(unitClass)
  }
}
