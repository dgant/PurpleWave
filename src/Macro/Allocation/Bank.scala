package Macro.Allocation

import Planning.Composition.ResourceLocks.LockCurrency
import Lifecycle.With

import scala.collection.mutable

class Bank {
  
  private var mineralsLeft  = 0
  private var gasLeft       = 0
  private var supplyLeft    = 0
  private val requests      = new mutable.HashSet[LockCurrency]()
  
  def update() {
    requests.clear()
    recountResources()
  }
  
  def prioritizedRequests: Iterable[LockCurrency] = {
    requests.toVector.sortBy(_.owner.priority)
  }
  
  def request(request: LockCurrency) {
    requests.add(request)
    recountResources()
  }
  
  def release(request: LockCurrency) {
    request.isSatisfied = false
    requests.remove(request)
    recountResources()
  }
  
  private def recountResources() {
    mineralsLeft  = With.self.minerals
    gasLeft       = With.self.gas
    supplyLeft    = With.self.supplyTotal - With.self.supplyUsed
    prioritizedRequests.foreach(queueBuyer)
  }
  
  private def queueBuyer(request: LockCurrency) {
    
    // If the request is so far in advance that we don't need to save money for it, then don't.
    //
    val framesToEarnCost = Math.max(framesToEarnMinerals(request.minerals), framesToEarnGas(request.gas))
    if (request.framesAhead > framesToEarnCost)
    {
      request.isSatisfied = false
      request.expectedFrames = expectedFrames(request)
    }
    else {
      request.isSatisfied = request.isSpent || isAvailableNow(request)
  
      if ( ! request.isSpent) {
        mineralsLeft -= request.minerals
        gasLeft      -= request.gas
        supplyLeft   -= request.supply
      }
  
      request.expectedFrames = expectedFrames(request)
    }
  }
  
  private def isAvailableNow(request: LockCurrency): Boolean = {
    (request.minerals == 0  ||  mineralsLeft  >= request.minerals) &&
    (request.gas      == 0  ||  gasLeft       >= request.gas)      &&
    (request.supply   == 0  ||  supplyLeft    >= request.supply)
  }
  
  private def framesToEarn(value: Int, rate: Double): Int = {
    if (value <= 0)
      0
    else if (rate <= 0.0)
      Int.MaxValue
    else
      Math.ceil(value / With.economy.ourIncomePerFrameMinerals).toInt
  }
  
  private def framesToEarnMinerals(minerals: Int): Int = {
    framesToEarn(minerals, With.economy.ourIncomePerFrameMinerals)
  }
  
  private def framesToEarnGas(gas: Int): Int = {
    framesToEarn(gas, With.economy.ourIncomePerFrameGas)
  }
  
  private def expectedFrames(request: LockCurrency): Int = {
    if (request.isSatisfied) return 0
    Vector(
      framesToEarnMinerals(-mineralsLeft),
      framesToEarnGas(-gasLeft)
    ).max
  }
}
