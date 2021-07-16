package Macro.MacroSim

import Lifecycle.With
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade
import Utilities.CountMap

import scala.collection.mutable

final class MacroSimState {
  var exists: Boolean = _
  var framesAhead: Int = _
  var minerals: Int = _
  var gas: Int = _
  var supplyAvailable: Int = _
  var supplyUsed: Int = _
  var mineralPatches: Int = _
  var geysers: Int = _
  val techs = new mutable.HashSet[Tech]
  val upgrades = new mutable.HashMap[Upgrade, Int]
  val units = new CountMap[UnitClass]
  val unitsAvailable = new CountMap[UnitClass]

  def reset(): Unit = {
    exists = false
    framesAhead = 0
    minerals = 0
    gas = 0
    supplyAvailable = 0
    supplyUsed = 0
    mineralPatches = 0
    geysers = 0
    techs.clear()
    upgrades.clear()
    units.clear()
    unitsAvailable.clear()
  }
  reset()

  // TODO: How many workers on minerals/gas?
  def mineralsPerFrame: Double = minerals * With.accounting.incomePerFrameMinerals
  def gasPerFrame: Double = geysers * With.accounting.incomePerFrameGas
}
