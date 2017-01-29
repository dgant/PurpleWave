package Processes.Allocation

import Startup.With
import Types.Contracts.{Buyer, ContractUnits, PriorityMultiplier}
import Types.Requirements.RequireUnits

import scala.collection.JavaConverters._
import scala.collection.mutable

class Recruiter {
  val _contracts:mutable.Set[ContractUnits] = mutable.Set.empty
  val _unassignedUnits:mutable.Set[bwapi.Unit] = mutable.Set.empty

  def tally(): Unit = {
    _unassignedUnits.clear()
    With.game.self.getUnits.asScala.filter(_.exists).filterNot(_contracts.flatten(_.units).contains).foreach(_unassignedUnits.add)
    _contracts.flatten(_.units).filterNot(_.exists).foreach(_removeFromContracts)
  }
  
  def getContract(
     requirement:RequireUnits,
     buyer: Buyer,
     priority: PriorityMultiplier):ContractUnits = {
  
    val contract = new ContractUnits(requirement, buyer, priority)
    _tryToFulfill(contract)
    contract
  }
  
  def releaseContract(contract: ContractUnits) {
    contract.units.foreach(unit => { _unassignedUnits.add(unit); _removeFromContracts(unit) })
    _contracts.remove(contract)
  }
  
  def _removeFromContracts(unit:bwapi.Unit) {
    _contracts.filter(_.units.contains(unit)).foreach(_.units.remove(unit))
  }
  
  def _tryToFulfill(contract:ContractUnits) {
    
    val candidates:mutable.Set[bwapi.Unit] = mutable.Set.empty
    contract.units.foreach(candidates.add)
    
    (_unassignedUnits ++ _contracts.filter(p => p.calculatePriority <= contract.calculatePriority).flatten(_.units))
      .filter(unit => candidates.size < contract.requirements.quantity)
      .filter(contract.requirements.unitMatcher.accept)
      .foreach(candidates.add(_))
    
    contract.requirementsMet = candidates.size >= contract.requirements.quantity
    
    //Slow but simple
    contract.units.clear()
    
    if (contract.requirementsMet) {
      candidates.foreach(contract.units.add(_))
    }
  }
}
