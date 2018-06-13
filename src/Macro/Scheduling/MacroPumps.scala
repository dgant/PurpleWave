package Macro.Scheduling

import ProxyBwapi.UnitClasses.UnitClass
import Utilities.CountMap

class MacroPumps {
  
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
