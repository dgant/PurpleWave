package Micro.Actions.Transportation

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ArrayBuffer

object Pickup extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.isTransport
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val passengersPotential = potentialPassengers(unit)
    if (passengersPotential.isEmpty && unit.loadedUnits.isEmpty && ! unit.unitClass.isDetector) {
      unit.agent.toTravel = Some(unit.agent.origin)
      Retreat.consider(unit)
      return
    }
    val passengersAccepted  = new ArrayBuffer[FriendlyUnitInfo]
    passengersPotential.toSeq.sortBy(_.framesToTravelTo(unit.pixel))
    
    var spaceRemaining = unit.spaceRemaining
    passengersPotential.foreach(passenger => {
      val spaceRequired = passenger.unitClass.spaceRequired
      if (spaceRequired <= spaceRemaining) {
        spaceRemaining -= spaceRequired
        passengersAccepted += passenger
        passenger.agent.toBoard = Some(unit)
        
        // Get in the chopper!
        Board.consider(passenger)
      }
    })
    
    if (passengersAccepted.nonEmpty) {
      val passengerCentroid = Maff.centroid(passengersAccepted.map(_.pixel))
      val passengerCentral  = passengersAccepted.minBy(_.pixelDistanceCenter(passengerCentroid))
      unit.agent.toTravel = Some(passengerCentral.pixel)
      Commander.move(unit)
    }
  }
  
  protected def potentialPassengers(unit: FriendlyUnitInfo): Iterable[FriendlyUnitInfo] = {
    unit.squad.map(_.units.filter(passenger =>
      unit.canTransport(passenger)
      && passenger.matchups.threatsInRange.isEmpty)).getOrElse(Iterable.empty)
  }
}
