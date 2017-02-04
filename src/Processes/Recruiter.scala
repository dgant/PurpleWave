package Processes

import Plans.Generic.Allocation.LockUnits
import Startup.With

import scala.collection.mutable

class Recruiter {
  val _assignments:mutable.HashMap[bwapi.Unit, LockUnits] = mutable.HashMap.empty
  val _unassigned:mutable.Set[bwapi.Unit] = mutable.Set.empty
  val _requests:mutable.HashMap[LockUnits, mutable.Set[bwapi.Unit]] = mutable.HashMap.empty
  val _updatedRequests:mutable.Set[LockUnits] = mutable.Set.empty

  def onFrame() {
    //Automatically free units held by dead requests
    _requests.keySet.diff(_updatedRequests).foreach(remove)
    _updatedRequests.clear()
    
    // Remove dead units
    //
    _assignments.keys.filterNot(_.exists).foreach(_unassign)
    _unassigned.filterNot(_.exists).foreach(_unassigned.remove)
    
    // Add new units
    //
    With.ourUnits.toSet.diff(_unassigned ++ _assignments.keys).foreach(_unassigned.add)
  }
  
  def getAssignment(unit:bwapi.Unit):Option[LockUnits] = {
    _assignments.get(unit)
  }
  
  def add(request: LockUnits) {
    _updatedRequests.add(request)
    _requests(request) = _requests.getOrElse(request, mutable.Set.empty)
    
    // Offer batches of units for the request to choose.
    //   Batch 0: Units not assigned
    //   Batch 1+: Units assigned to weaker-priority requests
    //
    val requiredUnits = request.getRequiredUnits(
      Iterable(_unassigned) ++
        _requests.keys
          .filter(otherRequest =>
            With.prioritizer.getPriority(request) <
            With.prioritizer.getPriority(otherRequest))
          .map(getUnits))

    if (requiredUnits == None) {
      request.isSatisfied = false
      remove(request)
    }
    else {
      request.isSatisfied = true
  
      // 1. Unassign all the current units
      // 2. Unassign all the required units
      // 3. Assign all the required units
      val unitsOld = getUnits(request)
      val unitsNew = requiredUnits.get
      unitsOld.foreach(_unassign)
      unitsNew.foreach(_unassign)
      unitsNew.foreach(_assign(_, request))
    }
  }
  
  def remove(request: LockUnits) {
    _requests.get(request).foreach(_.foreach(_unassign))
    _requests.remove(request)
  }
  
  def _assign(unit:bwapi.Unit, request:LockUnits) {
    _assignments(unit) = request
    _requests(request).add(unit)
    _unassigned.remove(unit)
  }
  
  def _unassign(unit:bwapi.Unit) {
    _unassigned.add(unit)
    _assignments.get(unit).foreach(_requests(_).remove(unit))
    _assignments.remove(unit)
  }
  
  def getUnits(request: LockUnits):mutable.Set[bwapi.Unit] = {
    _requests.getOrElse(request, mutable.Set.empty)
  }
}
