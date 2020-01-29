package Micro.Actions.Transportation

import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ArrayBuffer

object Pickup extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.isTransport
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val passengersPotential = potentialPassengers(unit)
    if (passengersPotential.isEmpty && unit.loadedUnits.isEmpty && ! unit.unitClass.isDetector) {
      unit.agent.toTravel = Some(unit.agent.origin)
      Avoid.consider(unit)
      return
    }
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
      val passengerCentroid = PurpleMath.centroid(passengersAccepted.map(_.pixelCenter))
      val passengerCentral  = passengersAccepted.minBy(_.pixelDistanceCenter(passengerCentroid))
      unit.agent.toTravel = Some(passengerCentral.pixelCenter)
      Move.delegate(unit)
    }
  }
  
  protected def potentialPassengers(unit: FriendlyUnitInfo): Iterable[FriendlyUnitInfo] = {
    unit.squad.map(_.units.filter(passenger =>
      ! passenger.zone.owner.isEnemy
      && unit.canTransport(passenger)
      && passenger.matchups.threatsInRange.isEmpty)).getOrElse(Iterable.empty)
  }
}
