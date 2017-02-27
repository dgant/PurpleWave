package Global.Allocation

import Plans.Allocation.LockUnits
import Startup.With
import Types.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Recruiter {
  val _requestByUnit:mutable.HashMap[FriendlyUnitInfo, LockUnits] = mutable.HashMap.empty
  val _unassignedUnits:mutable.Set[FriendlyUnitInfo] = mutable.Set.empty
  val _unitsByRequest:mutable.HashMap[LockUnits, mutable.Set[FriendlyUnitInfo]] = mutable.HashMap.empty
  val _updatedRequests:mutable.Set[LockUnits] = mutable.Set.empty

  def onFrame() {
    // Free units held by inactive requests
    _unitsByRequest.keySet.diff(_updatedRequests).foreach(_forgetRequest)
    _updatedRequests.clear()
    
    // Remove dead units
    _requestByUnit.keys.filterNot(_isEligible).foreach(_unassign)
    _unassignedUnits.filterNot(_isEligible).foreach(_unassignedUnits.remove)
    
    // Add new units
    With.units.ours
      .filter(_isEligible)
      .toSet
      .diff(_unassignedUnits ++ _requestByUnit.keys)
      .foreach(_unassignedUnits.add)
    
    //If we suspect any bugginess, enable this
    //_test
  }
  
  def _isEligible(unit:FriendlyUnitInfo):Boolean = {
    unit.alive && unit.complete
  }
  
  def _test {
    _unitsByRequest.foreach(pair1 =>
      _unitsByRequest.foreach(pair2 =>
        if (pair1 != pair2) {
          val intersection = pair1._2.intersect(pair2._2)
          if (intersection.nonEmpty) {
            With.logger.warn(pair1._1.toString + " and " + pair2._1.toString + " share " + intersection.size + " units")
          }
        }
      ))
  }
  
  def onUnitDestroyed(unit:FriendlyUnitInfo) {
    _unassign(unit)
    _unassignedUnits.remove(unit)
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
    val assignedToLowerPriority = _unitsByRequest.keys
      .filter(otherRequest =>
        With.prioritizer.getPriority(request) <
          With.prioritizer.getPriority(otherRequest))
      .map(getUnits)
    
    val requiredUnits = request.getRequiredUnits(Iterable(_unassignedUnits) ++ assignedToLowerPriority)

    if (requiredUnits == None) {
      request.isSatisfied = false
      _forgetRequest(request)
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
      
      unitsObsolete.foreach(_unassign)
      unitsNew.foreach(_unassign)
      unitsNew.foreach(_assign(_, request))
    }
  }
  
  def _forgetRequest(request: LockUnits) {
    _unitsByRequest.get(request).foreach(_.foreach(_unassign))
    _unitsByRequest.remove(request)
  }
  
  def _assign(unit:FriendlyUnitInfo, request:LockUnits) {
    _requestByUnit(unit) = request
    _unitsByRequest(request).add(unit)
    _unassignedUnits.remove(unit)
  }
  
  def _unassign(unit:FriendlyUnitInfo) {
    _unassignedUnits.add(unit)
    _requestByUnit.get(unit).foreach(request => _unitsByRequest.get(request).foreach(_.remove(unit)))
    _requestByUnit.remove(unit)
  }
  
  def getUnits(request: LockUnits):Set[FriendlyUnitInfo] = {
    _unitsByRequest.getOrElse(request, mutable.Set.empty).toSet
  }
}
