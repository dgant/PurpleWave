package Macro.Allocation

import Debugging.Visualizations.Views.Economy.ShowProduction
import Lifecycle.With
import Planning.ResourceLocks.LockCurrency
import Utilities.Time.{Forever, Seconds}

import scala.collection.mutable

class Bank {
  
  private var mineralsLeft    = 0
  private var gasLeft         = 0
  private var supplyLeft      = 0

  val requests = new mutable.PriorityQueue[LockCurrency]()(Ordering.by( - _.owner.priorityUntouched))
  var requestsLast: Seq[LockCurrency] = Seq.empty
  
  def update() {
    if (ShowProduction.inUse) {
      requestsLast = requests.toVector
    }
    requests.clear()
    recountResources()
  }
  
  def request(request: LockCurrency) {
    request.owner.prioritize()
    requests += request
    recountResources()
  }

  private val resourceLookaheadFrames = Seconds(2)()
  private def recountResources() {
    mineralsLeft  = With.self.minerals  + (resourceLookaheadFrames * With.accounting.ourIncomePerFrameMinerals).toInt
    gasLeft       = With.self.gas       + (resourceLookaheadFrames * With.accounting.ourIncomePerFrameGas).toInt
    supplyLeft    = With.self.supplyTotal400 - With.self.supplyUsed400
    requests.foreach(queueBuyer)
  }
  
  private def queueBuyer(request: LockCurrency) {
    val framesToEarnCost = Math.max(framesToEarnMinerals(request.minerals), framesToEarnGas(request.gas))
    request.satisfied = isAvailableNow(request)
    mineralsLeft -= request.minerals
    gasLeft      -= request.gas
    supplyLeft   -= request.supply
    request.expectedFrames = expectedFrames(request)
  }
  
  private def framesToEarn(value: Int, rate: Double): Int = {
    if (value <= 0) 0
    else if (rate <= 0.0) Forever()
    else Math.ceil(value / With.accounting.ourIncomePerFrameMinerals).toInt
  }
  
  private def framesToEarnMinerals(minerals: Int): Int = {
    framesToEarn(minerals, With.accounting.ourIncomePerFrameMinerals)
  }
  
  private def framesToEarnGas(gas: Int): Int = {
    framesToEarn(gas, With.accounting.ourIncomePerFrameGas)
  }

  private def isAvailableNow(request: LockCurrency): Boolean = {
    (request.minerals == 0  ||  mineralsLeft  >= request.minerals) &&
    (request.gas      == 0  ||  gasLeft       >= request.gas)      &&
    (request.supply   == 0  ||  supplyLeft    >= request.supply)
  }
  
  private def expectedFrames(request: LockCurrency): Int = {
    if (request.satisfied) return 0
    Math.max(
      if (request.minerals == 0) 0 else framesToEarnMinerals(-mineralsLeft),
      if (request.gas == 0) 0 else framesToEarnGas(-gasLeft))
  }
}
