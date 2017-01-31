package Processes.Allocation

import Startup.With
import Types.Requirements.RequireUnits

import scala.collection.JavaConverters._
import scala.collection.mutable

class Recruiter {
  val _assignments:mutable.HashMap[bwapi.Unit, RequireUnits] = mutable.HashMap.empty
  val _unassigned:mutable.Set[bwapi.Unit] = mutable.Set.empty
  val _requirements:mutable.HashMap[RequireUnits, mutable.Set[bwapi.Unit]] = mutable.HashMap.empty

  def tally() {
    
    //Remove dead units
    _assignments.keys.filterNot(_.exists).foreach(_unassign)
    _unassigned.filterNot(_.exists).foreach(_unassigned.remove)
    
    //Add new units
    With.game.self.getUnits.asScala.toSet.diff(_unassigned ++ _assignments.keys).foreach(_unassigned.add)
  }
  
  def fulfill(requirement: RequireUnits) {
    _requirements(requirement) = _requirements.getOrElse(requirement, mutable.Set.empty)
    
    //Offer batches of units for the requirement to choose
    //
    val requestedUnits = requirement.offerBatchesOfUnits(
      Iterable(_unassigned) ++
        _requirements.keys
          .filter(otherRequirement => requirement.priority > otherRequirement.priority)
          .map(getUnits))
  
    //This process is kind of goofy:
    //The requirement flags itself fulfilled if it likes the most recent offer
    //BEFORE the units are actually assigned
    //
    if (requirement.isFulfilled) {
      
      //Unassign any units we no longer want
      getUnits(requirement).diff(requestedUnits.toSet).foreach(_unassign)
      
      //Assign the requested units
      requestedUnits.foreach(unit => _assign(unit, requirement))
      
    } else {
      abort(requirement)
    }
  }
  
  def _assign(unit:bwapi.Unit, requirement:RequireUnits) {
    _assignments(unit) = requirement
    _requirements(requirement).add(unit)
    _unassigned.remove(unit)
  }
  
  def _unassign(unit:bwapi.Unit) {
    _unassigned.add(unit)
    _assignments.get(unit).foreach(_requirements(_).remove(unit))
    _assignments.remove(unit)
  }
  
  def abort(requirement: RequireUnits) {
     requirement.units.foreach(unit => { _unassigned.add(unit); _assignments.remove(unit) })
    _requirements.remove(requirement)
  }
  
  def getUnits(requirement: RequireUnits):mutable.Set[bwapi.Unit] = {
    _requirements.getOrElse(requirement, mutable.Set.empty)
  }
}
