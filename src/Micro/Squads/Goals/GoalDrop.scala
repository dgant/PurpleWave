package Micro.Squads.Goals

import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Utilities.ByOption

class GoalDrop(destination: Pixel) extends GoalBasic {
  
  override def toString: String = "Drop on " + destination.zone.name
  
  override def run() {
    val transports            = squad.recruits.filter   (_.isTransport)
    val convoys               = squad.recruits.filterNot(_.isTransport).filter(_.flying )
    val passengers            = squad.recruits.filterNot(_.isTransport).filter( ! _.flying )
    val passengersArrived     = passengers.filter(_.zone == destination.zone)
    val passengersDeparting   = passengers.filterNot(passengersArrived.contains)
    val keyTransport          = ByOption.minBy(transports)(_.pixelDistanceCenter(destination))
        
    val destinationTransports = destination
    val destinationConvoys    = if(keyTransport.isDefined) keyTransport.get.pixelCenter.project(destination, 32.0 * 3.0) else destination
    val destinationArrived    = destination
    val destinationDeparting  = if(keyTransport.isDefined) keyTransport.get.pixelCenter else destination
    
    
    transports.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(destinationTransports)
    }))
  
    convoys.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(destinationConvoys)
    }))
    
    passengersDeparting.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(destinationDeparting)
    }))
  
    passengersArrived.foreach(_.agent.intend(squad.client, new Intention {
      toTravel    = Some(destinationArrived)
      canBerzerk  = true
    }))
    
    convoys.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(destination)
    }))
  }
  
  override def acceptsHelp: Boolean = false
}
