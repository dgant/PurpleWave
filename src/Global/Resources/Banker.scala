package Global.Resources

import Plans.Allocation.LockCurrency
import Startup.With

import scala.collection.mutable

class Banker {
  var _mineralsLeft = 0
  var _gasLeft = 0
  var _supplyLeft = 0
  val _requests = new mutable.HashSet[LockCurrency]()
  
  def onFrame() {
    _requests.clear()
    recountResources()
  }
  
  def getPrioritizedRequests:Iterable[LockCurrency] = {
    _requests.toSeq.sortBy(With.prioritizer.getPriority(_))
  }
  
  def recountResources() {
    _mineralsLeft  = With.self.minerals
    _gasLeft       = With.self.gas
    _supplyLeft    = With.self.supplyTotal - With.self.supplyUsed
    getPrioritizedRequests.foreach(_queueBuyer)
  }
  
  def add(request:LockCurrency) {
    _requests.add(request)
    recountResources()
  }
  
  def remove(request:LockCurrency) {
    request.isSatisfied = false
    _requests.remove(request)
    recountResources()
  }
  
  def _queueBuyer(request:LockCurrency) {
    request.isSatisfied = request.isSpent || _isAvailableNow(request)
    
    if ( ! request.isSpent) {
      _mineralsLeft -= request.minerals
      _gasLeft      -= request.gas
      _supplyLeft   -= request.supply
    }
  }
  
  def _isAvailableNow(request:LockCurrency): Boolean = {
    (request.minerals == 0  ||  _mineralsLeft  >= request.minerals) &&
    (request.gas      == 0  ||  _gasLeft       >= request.gas)      &&
    (request.supply   == 0  ||  _supplyLeft    >= request.supply)
  }
}
