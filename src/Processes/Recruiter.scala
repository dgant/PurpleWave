package Processes

import Plans.Generic.Allocation.LockUnits
import Startup.With

import scala.collection.mutable

class Recruiter {
  val _requestByUnit:mutable.HashMap[Int, LockUnits] = mutable.HashMap.empty
  val _unassignedUnits:mutable.Set[Int] = mutable.Set.empty
  val _unitsByRequest:mutable.HashMap[LockUnits, mutable.Set[Int]] = mutable.HashMap.empty
  val _updatedRequests:mutable.Set[LockUnits] = mutable.Set.empty

  def onFrame() {
    //Automatically free buildersOccupied held by dead requests
    _unitsByRequest.keySet.diff(_updatedRequests).foreach(remove)
    _updatedRequests.clear()
    
    // Remove dead buildersOccupied
    //
    _requestByUnit.keys.filterNot(id => With.unit(id).exists(u => u.exists)).foreach(_unassign)
    _unassignedUnits.filterNot(id => With.unit(id).exists(u => u.exists)).foreach(_unassignedUnits.remove)
    
    // Add new buildersOccupied
    //
    With.ourUnits.map(_.getID).diff(_unassignedUnits ++ _requestByUnit.keys).foreach(_unassignedUnits.add)
  }
  
  def onUnitDestroyed(unit:bwapi.Unit) {
    _unassign(unit.getID)
    _unassignedUnits.remove(unit.getID)
  }
  
  def getAssignment(id:Int):Option[LockUnits] = {
    _requestByUnit.get(id)
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
    
    // Offer batches of buildersOccupied for the request to choose.
    //   Batch 0: Units not assigned
    //   Batch 1+: Units assigned to weaker-priority requests
    //
    val unassignedUnits = _unassignedUnits.map(With.unit).flatten
    val assignedToLowerPriority = _unitsByRequest.keys
      .filter(otherRequest =>
        With.prioritizer.getPriority(request) <
          With.prioritizer.getPriority(otherRequest))
      .map(getUnits)
    
    val requiredUnits = request.getRequiredUnits(Iterable(unassignedUnits) ++ assignedToLowerPriority)

    if (requiredUnits == None) {
      request.isSatisfied = false
      remove(request)
    }
    else {
      request.isSatisfied = true
  
      // 1. Unassign all the current buildersOccupied
      // 2. Unassign all the required buildersOccupied
      // 3. Assign all the required buildersOccupied
      val unitsBefore = getUnits(request)
      val unitsAfter = requiredUnits.get.toSet
      val unitsObsolete = unitsBefore.diff(unitsAfter)
      val unitsNew = unitsAfter.diff(unitsBefore)
      
      unitsObsolete.map(_.getID).foreach(_unassign)
      unitsNew.map(_.getID).foreach(_unassign)
      unitsNew.map(_.getID).foreach(_assign(_, request))
    }
  }
  
  def remove(request: LockUnits) {
    _unitsByRequest.get(request).foreach(_.foreach(_unassign))
    _unitsByRequest.remove(request)
  }
  
  def _assign(id:Int, request:LockUnits) {
    _requestByUnit(id) = request
    _unitsByRequest(request).add(id)
    _unassignedUnits.remove(id)
  }
  
  def _unassign(id:Int) {
    _unassignedUnits.add(id)
    _requestByUnit.get(id).foreach(lock => _unitsByRequest.get(lock).foreach(unitSet => unitSet.remove(id)))
    _requestByUnit.remove(id)
  }
  
  def getUnits(request: LockUnits):Set[bwapi.Unit] = {
    _unitsByRequest.getOrElse(request, mutable.Set.empty).map(With.unit).flatten.toSet
  }
}
