package Macro.Scheduling

import Lifecycle.With
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable

class MasterBuildOrderHistory {
  
  private val countByClassAllTime = new mutable.HashMap[UnitClass, mutable.HashSet[Int]] {
    override def default(key: UnitClass): mutable.HashSet[Int] = {
      if ( ! contains(key)) put(key, new mutable.HashSet[Int])
      this(key)
    }
  }
  
  def doneAllTime(unitClass: UnitClass): Int = countByClassAllTime(unitClass).size
  
  def update() {
    With.units.ours.foreach(unit => countByClassAllTime(unit.unitClass).add(unit.id))
  }
}
