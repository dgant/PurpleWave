package Processes.Allocation

import Startup.With
import Types.Contracts.{Buyer, ContractCurrency, PriorityMultiplier}
import Types.Requirements.RequireCurrency

import scala.collection.mutable

class Banker {
  var _minerals = 0
  var _gas = 0
  var _supply = 0
  val _activeContracts:mutable.Set[ContractCurrency] = mutable.Set.empty
  
  def tally() {
    _minerals  = With.game.self.minerals
    _gas       = With.game.self.gas
    _supply    = With.game.self.supplyTotal - With.game.self.supplyUsed
    
    _activeContracts.toSeq
      .sortBy(c => c.buyer.priority * c.priority.multiplier)
      .foreach(_deductContractValue)
  }
  
  def getContract(
    requirement:RequireCurrency,
    buyer: Buyer,
    priorityMultiplier: PriorityMultiplier):ContractCurrency = {
  
    val contract = new ContractCurrency(
      requirement,
      buyer,
      priorityMultiplier)
      
    _activeContracts.add(contract)
    tally()
    contract
  }
  
  def releaseContract(contract:ContractCurrency) {
    contract.requirementsMet = false
    _activeContracts.remove(contract)
    tally()
  }
  
  def _deductContractValue(contract: ContractCurrency) {
    contract.requirementsMet = _isAvailableNow(
      contract.requirements,
      contract.buyer,
      contract.priority)
    
    _minerals -= contract.requirements.minerals
    _gas -= contract.requirements.gas
    _supply -= contract.requirements.supply
  }
  
  def _isAvailableNow(
    requirement:RequireCurrency,
    buyer: Buyer,
    priorityMultiplier: PriorityMultiplier): Boolean = {
    
    _minerals  >= requirement.minerals &&
    _gas       >= requirement.gas &&
    _supply    >= requirement.supply
  }
}
