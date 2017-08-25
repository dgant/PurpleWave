package Micro.Actions.Transportation

import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ArrayBuffer
import Utilities.EnrichPixel.EnrichedPixelCollection

object Pickup extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.isTransport &&
    potentialPassengers(unit).nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val passengersPotential = potentialPassengers(unit)
    val passengersAccepted  = new ArrayBuffer[FriendlyUnitInfo]
    passengersPotential.toSeq.sortBy(_.framesToTravelTo(unit.pixelCenter))
    
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
      val passengerCentroid = passengersAccepted.map(_.pixelCenter).centroid
      val passengerCentral  = passengersAccepted.minBy(_.pixelDistanceFast(passengerCentroid))
      unit.agent.toTravel = Some(passengerCentral.pixelCenter)
      Move.delegate(unit)
    }
  }
  
  protected def potentialPassengers(unit: FriendlyUnitInfo): Iterable[FriendlyUnitInfo] = {
    unit.squad.map(_.recruits.filter(passenger =>
      unit.canTransport(passenger) &&
      passenger.matchups.threatsInRange.isEmpty)).getOrElse(Iterable.empty)
  }
}
