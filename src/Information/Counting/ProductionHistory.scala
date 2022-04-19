package Information.Counting

import Lifecycle.With
import Performance.Tasks.TimedTask
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable

class ProductionHistory extends TimedTask {
  
  private val countByClassAllTime = new mutable.HashMap[UnitClass, mutable.HashSet[Int]] {
    override def default(key: UnitClass): mutable.HashSet[Int] = {
      if ( ! contains(key)) put(key, new mutable.HashSet[Int])
      this(key)
    }
  }
  
  def doneAllTime(unitClass: UnitClass): Int = countByClassAllTime(unitClass).size

  override protected def onRun(budgetMs: Long): Unit = {
    With.units.ours.foreach(unit => MacroCounter.countComplete(unit).foreach(p => countByClassAllTime(p._1).add(unit.id)))
  }
}
