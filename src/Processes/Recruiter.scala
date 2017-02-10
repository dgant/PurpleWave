package Processes

import Plans.Generic.Allocation.LockUnits
import Startup.With

import scala.collection.mutable

class Recruiter {
  val _requestByUnit:mutable.HashMap[bwapi.Unit, LockUnits] = mutable.HashMap.empty
  val _unassignedUnits:mutable.Set[bwapi.Unit] = mutable.Set.empty
  val _unitsByRequest:mutable.HashMap[LockUnits, mutable.Set[bwapi.Unit]] = mutable.HashMap.empty
  val _updatedRequests:mutable.Set[LockUnits] = mutable.Set.empty

  def onFrame() {
    //Automatically free units held by dead requests
    _unitsByRequest.keySet.diff(_updatedRequests).foreach(remove)
    _updatedRequests.clear()
    
    // Remove dead units
    //
    _requestByUnit.keys.filterNot(_.exists).foreach(_unassign)
    _unassignedUnits.filterNot(_.exists).foreach(_unassignedUnits.remove)
    
    // Add new units
    //
    With.ourUnits.diff(_unassignedUnits ++ _requestByUnit.keys).foreach(_unassignedUnits.add)
  }
  
  def onUnitDestroyed(unit:bwapi.Unit) {
    _unassign(unit)
    _unassignedUnits.remove(unit)
  }
  
  def getAssignment(unit:bwapi.Unit):Option[LockUnits] = {
    _requestByUnit.get(unit)
  }
  
  def add(request: LockUnits) {
    //This lock is already up to date. Chill.
    if (_updatedRequests.contains(request)) {
      //  return
    }
  
    _updatedRequests.add(request)
    _unitsByRequest(request) = _unitsByRequest.getOrElse(request, mutable.Set.empty)
    _tryToSatisfy(request)
  }
  
  def _tryToSatisfy(request: LockUnits) {
    
    // Offer batches of units for the request to choose.
    //   Batch 0: Units not assigned
    //   Batch 1+: Units assigned to weaker-priority requests
    //
    val requiredUnits = request.getRequiredUnits(
      Iterable(_unassignedUnits) ++
        _unitsByRequest.keys
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
      val unitsBefore = getUnits(request)
      val unitsAfter = requiredUnits.get.toSet
      val unitsObsolete = unitsBefore.diff(unitsAfter)
      val unitsNew = unitsAfter.diff(unitsBefore)
      
      unitsObsolete.foreach(_unassign)
      unitsNew.foreach(_unassign)
      unitsNew.foreach(_assign(_, request))
    }
  }
  
  def remove(request: LockUnits) {
    _unitsByRequest.get(request).foreach(_.foreach(_unassign))
    _unitsByRequest.remove(request)
  }
  
  def _assign(unit:bwapi.Unit, request:LockUnits) {
    _requestByUnit(unit) = request
    _unitsByRequest(request).add(unit)
    _unassignedUnits.remove(unit)
  }
  
  def _unassign(unit:bwapi.Unit) {
    _unassignedUnits.add(unit)
    _requestByUnit.get(unit).foreach(lock => _unitsByRequest.get(lock).foreach(unitSet => unitSet.remove(unit)))
    _requestByUnit.remove(unit)
  }
  
  def getUnits(request: LockUnits):mutable.Set[bwapi.Unit] = {
    _unitsByRequest.getOrElse(request, mutable.Set.empty)
  }
}
