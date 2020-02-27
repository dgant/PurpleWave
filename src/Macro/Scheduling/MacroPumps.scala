package Macro.Scheduling

import ProxyBwapi.UnitClasses.UnitClass
import Utilities.CountMap

class MacroPumps {
  
  private val builderConsumed: CountMap[UnitClass] = new CountMap[UnitClass]
  private val buildeePumped: CountMap[UnitClass] = new CountMap[UnitClass]
  
  def reset() {
    builderConsumed.clear()
    buildeePumped.clear()
  }
  
  def consume(unitClass: UnitClass, count: Int) {
    builderConsumed(unitClass) += count
  }

  def pump(unitClass: UnitClass, count: Int): Unit = {
    buildeePumped(unitClass) += count
  }

  def consumeUpTo(unitClass: UnitClass, count: Int) {
    builderConsumed(unitClass) = Math.max(builderConsumed(unitClass), count)
  }

  def buildUpTo(unitClass: UnitClass, count: Int): Unit = {
    buildeePumped(unitClass) = Math.max(buildeePumped(unitClass), count)
  }
  
  def pumpsConsumed(unitClass: UnitClass): Int = {
    builderConsumed(unitClass)
  }

  def buildeesPumped(unitClass: UnitClass): Int = {
    buildeePumped(unitClass)
  }
}
