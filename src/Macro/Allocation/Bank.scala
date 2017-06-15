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
    requests.toVector.sortBy(request => With.prioritizer.getPriority(request.owner))
  }
  
  def request(request:LockCurrency) {
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
    request.isSatisfied = request.isSpent || isAvailableNow(request)
    
    if ( ! request.isSpent) {
      mineralsLeft -= request.minerals
      gasLeft      -= request.gas
      supplyLeft   -= request.supply
    }
    
    request.expectedFrames = expectedFrames(request)
  }
  
  private def isAvailableNow(request: LockCurrency): Boolean = {
    (request.minerals == 0  ||  mineralsLeft  >= request.minerals) &&
    (request.gas      == 0  ||  gasLeft       >= request.gas)      &&
    (request.supply   == 0  ||  supplyLeft    >= request.supply)
  }
  
  private def expectedFrames(request: LockCurrency): Int = {
    if (request.isSatisfied) return 0
    Vector(
      0,
      if (request.minerals == 0)
        0
      else if (With.economy.ourIncomePerFrameMinerals == 0)
        Int.MaxValue
      else
        -mineralsLeft / With.economy.ourIncomePerFrameMinerals,
      if (request.gas == 0)
        0
      else if (With.economy.ourIncomePerFrameGas == 0)
        Int.MaxValue
      else
        -gasLeft / With.economy.ourIncomePerFrameGas
    ).min.toInt
  }
}
