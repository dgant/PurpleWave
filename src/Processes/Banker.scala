package Processes

import Startup.With
import Types.Requirements.RequireCurrency

import scala.collection.mutable

class Banker {
  var _minerals = 0
  var _gas = 0
  var _supply = 0
  val _requirements:mutable.Set[RequireCurrency] = mutable.Set.empty
  
  def startFrame() {
    _minerals  = With.game.self.minerals
    _gas       = With.game.self.gas
    _supply    = With.game.self.supplyTotal - With.game.self.supplyUsed
    _requirements.toSeq.sortBy(- _.priority).foreach(_deductContractValue)
  }
  
  def fulfill(requirement:RequireCurrency) {
    _requirements.add(requirement)
    startFrame()
  }
  
  def abort(requirement:RequireCurrency) {
    _requirements.remove(requirement)
    startFrame()
  }
  
  def _deductContractValue(requirement: RequireCurrency) {
    requirement.isAvailableNow = _isAvailableNow(requirement)
    _minerals -= requirement.minerals
    _gas      -= requirement.gas
    _supply   -= requirement.supply
  }
  
  def _isAvailableNow(requirement:RequireCurrency): Boolean = {
    _minerals  >= requirement.minerals &&
    _gas       >= requirement.gas &&
    _supply    >= requirement.supply
  }
}
