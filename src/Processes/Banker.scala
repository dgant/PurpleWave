package Processes

import Startup.With
import Types.Plans.Generic.Allocation.PlanAcquireCurrency

import scala.collection.mutable

class Banker {
  var _mineralsLeft = 0
  var _gasLeft = 0
  var _supplyLeft = 0
  val _requests = new mutable.HashSet[PlanAcquireCurrency]()
  
  def recountResources() {
    _mineralsLeft  = With.game.self.minerals
    _gasLeft       = With.game.self.gas
    _supplyLeft    = With.game.self.supplyTotal - With.game.self.supplyUsed
    _requests.toSeq.sortBy(With.prioritizer.getPriority(_)).foreach(_queueBuyer)
  }
  
  def add(request:PlanAcquireCurrency) {
    _requests.add(request)
    recountResources()
  }
  
  def remove(request:PlanAcquireCurrency) {
    request.requestFulfilled = false
    _requests.remove(request)
    recountResources()
  }
  
  def _queueBuyer(request:PlanAcquireCurrency) {
    request.requestFulfilled = request.isSpent || _isAvailableNow(request)
    
    if ( ! request.isSpent) {
      _mineralsLeft -= request.minerals
      _gasLeft      -= request.gas
      _supplyLeft   -= request.supply
    }
  }
  
  def _isAvailableNow(request:PlanAcquireCurrency): Boolean = {
    (request.minerals == 0  ||  _mineralsLeft  >= request.minerals) &&
    (request.gas      == 0  ||  _gasLeft       >= request.gas)      &&
    (request.supply   == 0  ||  _supplyLeft    >= request.supply)
  }
}
