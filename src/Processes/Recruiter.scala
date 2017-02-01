package Processes

import Startup.With
import Types.Traits.UnitRequest

import scala.collection.JavaConverters._
import scala.collection.mutable

class Recruiter {
  val _assignments:mutable.HashMap[bwapi.Unit, UnitRequest] = mutable.HashMap.empty
  val _unassigned:mutable.Set[bwapi.Unit] = mutable.Set.empty
  val _requests:mutable.HashMap[UnitRequest, mutable.Set[bwapi.Unit]] = mutable.HashMap.empty

  def recountUnits() {
    // Remove dead units
    //
    _assignments.keys.filterNot(_.exists).foreach(_unassign)
    _unassigned.filterNot(_.exists).foreach(_unassigned.remove)
    
    // Add new units
    //
    With.game.self.getUnits.asScala.toSet.diff(_unassigned ++ _assignments.keys).foreach(_unassigned.add)
  }
  
  def add(request: UnitRequest) {
    _requests(request) = _requests.getOrElse(request, mutable.Set.empty)
    
    // Offer batches of units for the request to choose.
    //   Batch 0: Units not assigned
    //   Batch 1+: Units assigned to weaker-priority requests
    //
    val requiredUnits = request.getRequiredUnits(
      Iterable(_unassigned) ++
        _requests.keys
          .filter(otherRequest => request.priority > otherRequest.priority)
          .map(getUnits))
  
    // This process is kind of goofy:
    // The request flags itself fulfilled if it likes the most recent offer
    // BEFORE the units are actually assigned
    //
    if (requiredUnits == None) {
      request.requestFulfilled = false
      remove(request)
    }
    else {
      request.requestFulfilled = true
  
      // Unassign any units we no longer want
      //
      getUnits(request).diff(requiredUnits.get.toSet).foreach(_unassign)
  
      //Assign the requested units
      //
      requiredUnits.get.foreach(unit => _assign(unit, request))
    }
  }
  
  def remove(request: UnitRequest) {
    _requests(request).foreach(unit => { _unassigned.add(unit); _assignments.remove(unit) })
    _requests.remove(request)
  }
  
  def _assign(unit:bwapi.Unit, request:UnitRequest) {
    _assignments(unit) = request
    _requests(request).add(unit)
    _unassigned.remove(unit)
  }
  
  def _unassign(unit:bwapi.Unit) {
    _unassigned.add(unit)
    _assignments.get(unit).foreach(_requests(_).remove(unit))
    _assignments.remove(unit)
  }
  
  def getUnits(request: UnitRequest):mutable.Set[bwapi.Unit] = {
    _requests.getOrElse(request, mutable.Set.empty)
  }
}
