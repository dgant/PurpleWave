package Macro.Allocation

import Planning.Composition.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran}
import Startup.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Recruiter {
  
  private val requestByUnit   : mutable.HashMap[FriendlyUnitInfo, LockUnits]              = mutable.HashMap.empty
  private val unassignedUnits : mutable.Set[FriendlyUnitInfo]                             = mutable.Set.empty
  private val unitsByRequest  : mutable.HashMap[LockUnits, mutable.Set[FriendlyUnitInfo]] = mutable.HashMap.empty
  private val updatedRequests : mutable.Set[LockUnits]                                    = mutable.Set.empty

  def onFrame() {
    // Free units held by inactive requests
    unitsByRequest.keySet.diff(updatedRequests).foreach(forgetRequest)
    updatedRequests.clear()
    
    // Remove dead units
    requestByUnit.keys.filterNot(isEligible).foreach(unassign)
    unassignedUnits.filterNot(isEligible).foreach(unassignedUnits.remove)
    
    // Add new units
    With.units.ours
      .filter(isEligible)
      .diff(unassignedUnits ++ requestByUnit.keys)
      .foreach(unassignedUnits.add)
    
    //If we suspect any bugginess, enable this
    //test
  }
  
  val ineligibleClasses = Set(Protoss.Interceptor, Protoss.Scarab, Terran.SpiderMine)
  private def isEligible(unit:FriendlyUnitInfo):Boolean =
    unit.alive &&
    unit.complete &&
    ! ineligibleClasses.contains(unit.unitClass)
  
  private def test {
    unitsByRequest.foreach(pair1 =>
      unitsByRequest.foreach(pair2 =>
        if (pair1 != pair2) {
          val intersection = pair1._2.intersect(pair2._2)
          if (intersection.nonEmpty) {
            With.logger.warn(pair1._1.toString + " and " + pair2._1.toString + " share " + intersection.size + " units")
          }
        }
      ))
  }
  
  def onUnitDestroyed(unit:FriendlyUnitInfo) {
    unassign(unit)
    unassignedUnits.remove(unit)
  }
  
  def add(request: LockUnits) {
    //This lock is already up to date. Chill.
    if (updatedRequests.contains(request)) {
      //  return
    }
  
    updatedRequests.add(request)
    unitsByRequest(request) = unitsByRequest.getOrElse(request, mutable.Set.empty)
    tryToSatisfy(request)
  }
  
  private def tryToSatisfy(request: LockUnits) {
    
    // Offer batches of buildersOccupied for the request to choose.
    //   Batch 0: Units not assigned
    //   Batch 1+: Units assigned to weaker-priority requests
    //
    val assignedToLowerPriority = unitsByRequest.keys
      .filter(otherRequest =>
        With.prioritizer.getPriority(request.owner) <
          With.prioritizer.getPriority(otherRequest.owner))
      .map(getUnits)
    
    val requiredUnits = request.offerUnits(Iterable(unassignedUnits) ++ assignedToLowerPriority)

    if (requiredUnits == None) {
      forgetRequest(request)
    }
    else {
  
      // 1. Unassign all the current buildersOccupied
      // 2. Unassign all the required buildersOccupied
      // 3. Assign all the required buildersOccupied
      val unitsBefore = getUnits(request)
      val unitsAfter = requiredUnits.get.toSet
      val unitsObsolete = unitsBefore.diff(unitsAfter)
      val unitsNew = unitsAfter.diff(unitsBefore)
      
      unitsObsolete.foreach(unassign)
      unitsNew.foreach(unassign)
      unitsNew.foreach(assign(_, request))
    }
  }
  
  private def forgetRequest(request: LockUnits) {
    unitsByRequest.get(request).foreach(_.foreach(unassign))
    unitsByRequest.remove(request)
  }
  
  private def assign(unit:FriendlyUnitInfo, request:LockUnits) {
    requestByUnit(unit) = request
    unitsByRequest(request).add(unit)
    unassignedUnits.remove(unit)
  }
  
  private def unassign(unit:FriendlyUnitInfo) {
    unassignedUnits.add(unit)
    requestByUnit.get(unit).foreach(request => unitsByRequest.get(request).foreach(_.remove(unit)))
    requestByUnit.remove(unit)
  }
  
  def getUnits(request: LockUnits):Set[FriendlyUnitInfo] = {
    unitsByRequest.getOrElse(request, mutable.Set.empty).toSet
  }
}
