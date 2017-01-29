package Processes

import Startup.With
import Types.Contracts.{Buyer, ContractUnits, PriorityMultiplier}
import Types.Requirements.RequireUnits

import scala.collection.JavaConverters._
import scala.collection.mutable

class Recruiter {
  val contracts:mutable.Set[ContractUnits] = mutable.Set.empty
  var unassignedUnits:mutable.Set[bwapi.Unit] = mutable.Set.empty

  def tally(): Unit = {
    unassignedUnits = mutable.Set.empty
    With.game.self.getUnits.asScala.filter(_.exists).filterNot(contracts.flatten(_.employees).contains).foreach(unassignedUnits.add)
    contracts.flatten(_.employees).filterNot(_.exists).foreach(_fire)
  }
  
  def getUnemployed():Iterable[bwapi.Unit] = {
    unassignedUnits
  }
  
  def getContract(
     requirement:RequireUnits,
     buyer: Buyer,
     priority: PriorityMultiplier):ContractUnits = {
  
    val contract = new ContractUnits(requirement, buyer, priority)
    Interviewer.tryToFulfill(contract)
    contract
  }
  
  def releaseContract(contract: ContractUnits) {
    contract.employees.foreach(_fire)
    contracts.remove(contract)
  }
  
  def _fire(firedEmployee:bwapi.Unit) {
    assignments.get(firedEmployee).foreach(contract => contract.employees.remove(firedEmployee))
  }
  
  def _updateContractActivity(contract:ContractUnits) {
    
  }
}
