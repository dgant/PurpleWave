package Processes

import Types.Contracts.ContractUnits

import scala.collection.mutable

object Interviewer {
  def tryToFulfill(contract:ContractUnits) {
    
    val candidates:mutable.Set[bwapi.Unit] = mutable.Set.empty
    contract.employees.foreach(candidates.add)
    
    
    //Pull from unemployed
    //Pull from lowest priority contracts
  
    return Some(candidates)
    
    None
  }
  
  def testRequirements(contract:ContractUnits, units:Iterable[Unit]):Boolean = {
      
  }
}
