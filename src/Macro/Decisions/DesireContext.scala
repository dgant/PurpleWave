package Macro.Decisions

import Lifecycle.With
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

class DesireContext {
  
  private def countTypes(units: Iterable[UnitInfo]): Map[UnitClass, Int] = {
    val output = units.groupBy(_.unitClass).map(x => (x._1, x._2.size))
    output
  }
  
  val enemyUnits  : Map[UnitClass, Int] = countTypes(With.units.enemy)
  val ourUnits    : Map[UnitClass, Int] = countTypes(With.units.ours)
  val minerals    : Double              = With.self.minerals  + 24 * 60 * With.economy.ourIncomePerFrameMinerals
  val gas         : Double              = With.self.gas       + 24 * 60 * With.economy.ourIncomePerFrameGas
}