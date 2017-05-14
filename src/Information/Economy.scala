package Information

import Information.Geography.Types.Base
import Lifecycle.With
import Micro.Actions.Basic.Gather
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class Economy {
  
  private val incomePerFrameMinerals  = 0.040
  private val incomePerFrameGas       = 0.066
  
  //Should start at 50, of course but this -30 offsets the effect of starting workers all being far from minerals
  var ourEstimatedTotalMinerals = 20.0
  var ourEstimatedTotalGas = 0.0
  
  var _lastFrame = 0
  def update() = {
    val frameDiff = With.frame - _lastFrame
    ourEstimatedTotalMinerals += frameDiff * ourIncomePerFrameMinerals
    ourEstimatedTotalGas      += frameDiff * ourIncomePerFrameGas
    _lastFrame = With.frame
  }
  
  def ourActualTotalMinerals    : Integer = With.self.gatheredMinerals
  def ourActualTotalGas         : Integer = With.self.gatheredGas
  
  def ourIncomePerFrameMinerals : Double = ourActiveMiners.size   * incomePerFrameMinerals
  def ourIncomePerFrameGas      : Double = ourActiveDrillers.size * incomePerFrameGas
  
  def genericIncomePerFrameMinerals (miners:Int, bases:Int): Double = Math.min(miners, bases * 16)  * incomePerFrameMinerals
  def genericIncomePerFrameGas      (miners:Int, bases:Int): Double = Math.min(miners, bases * 3)   * incomePerFrameGas
  
  def ourActiveGatherers  : Traversable[FriendlyUnitInfo] = With.geography.ourBases.flatten(ourActiveGatherers)
  def ourActiveMiners     : Traversable[FriendlyUnitInfo] = With.geography.ourBases.flatten(ourActiveMiners)
  def ourActiveDrillers   : Traversable[FriendlyUnitInfo] = With.geography.ourBases.flatten(ourActiveDrillers)
  
  def ourActiveGatherers(base:Base):Traversable[FriendlyUnitInfo] = {
    With.units
      .inRectangle(base.harvestingArea)
      .flatten(_.friendly)
      .filter(unit => unit.unitClass.isWorker && unit.executionState.lastAction.contains(Gather))
  }
  
  def ourActiveMiners(base:Base):Traversable[FriendlyUnitInfo] = {
    ourActiveGatherers(base).filter(_.gatheringMinerals).take(base.minerals.size * 2)
  }
  
  def ourActiveDrillers(base:Base):Traversable[FriendlyUnitInfo] = {
    ourActiveGatherers(base).filter(_.gatheringGas).take(base.gas.filter(_.isOurs).size * 3)
  }
}
