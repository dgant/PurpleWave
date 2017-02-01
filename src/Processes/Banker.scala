package Processes

import Startup.With
import Types.Traits.CurrencyRequest

import scala.collection.mutable

class Banker {
  var _mineralsLeft = 0
  var _gasLeft = 0
  var _supplyLeft = 0
  val _requests = new mutable.HashSet[CurrencyRequest]()
  
  def recountResources() {
    _mineralsLeft  = With.game.self.minerals
    _gasLeft       = With.game.self.gas
    _supplyLeft    = With.game.self.supplyTotal - With.game.self.supplyUsed
    _requests.toSeq.sortBy(- _.priority).foreach(_queueBuyer)
  }
  
  def add(request:CurrencyRequest) {
    _requests.add(request)
    recountResources()
  }
  
  def remove(request:CurrencyRequest) {
    request.requestFulfilled = false
    _requests.remove(request)
    recountResources()
  }
  
  def _queueBuyer(request:CurrencyRequest) {
    request.requestFulfilled = _isAvailableNow(request)
    _mineralsLeft -= request.minerals
    _gasLeft      -= request.gas
    _supplyLeft   -= request.supply
  }
  
  def _isAvailableNow(request:CurrencyRequest): Boolean = {
    _mineralsLeft  >= request.minerals &&
    _gasLeft       >= request.gas &&
    _supplyLeft    >= request.supply
  }
}
