package Processes

import Startup.With
import Types.Allocations.Contract
import Types.Resources.JobDescription

import scala.collection.JavaConverters._
import scala.collection.mutable

class Recruiter {
  val contracts:mutable.Set[Contract] = mutable.Set.empty
  val assignments:mutable.Map[bwapi.Unit, Contract] = mutable.Map.empty

  def headcount(): Unit = {
    With.game.self.getUnits.asScala.filterNot(_.exists).foreach(_fire)
  }
  
  //This is going to be slow
  def getUnemployed():Iterable[bwapi.Unit] = {
    With.game.self.getUnits.asScala.filterNot(assignments.contains)
  }
  
  //For now, always hire greedily
  def source(jobDescription:JobDescription):Option[Contract] = {
    val recruits = Interviewer.hunt(jobDescription, hireGreedily = true)
    if (recruits == None) {
      return None
    }
    
    val contract = new Contract(jobDescription)
    contracts.add(contract)
    recruits.get.foreach(recruit => {
      _fire(recruit)
      contract.employees.add(recruit)
      assignments.put(recruit, contract)
    })
    Some(contract)
  }
  
  def _terminate(contract: Contract) {
    contract.employees.foreach(_fire)
    contracts.remove(contract)
  }
  
  def _fire(firedEmployee:bwapi.Unit) {
    assignments.get(firedEmployee).foreach(contract => contract.employees.remove(firedEmployee))
  }
}
