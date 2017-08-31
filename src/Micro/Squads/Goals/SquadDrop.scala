package Micro.Squads.Goals

import Mathematics.Points.Pixel
import Micro.Agency.Intention

class SquadDrop(destination: Pixel) extends SquadGoal {
  
  override def toString: String = "Drop on " + destination.zone.name
  
  def updateUnits() {
    val transports  = squad.recruits.filter   (_.isTransport)
    val passengers  = squad.recruits.filterNot(_.isTransport).filter( ! _.flying )
    val convoys     = squad.recruits.filterNot(_.isTransport).filter(_.flying )
        
    squad.recruits.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(destination)
    }))
    
    if (transports.nonEmpty) {
      val keyTransport = transports.minBy(_.pixelDistanceFast(destination))
      passengers.foreach(passenger => {
        passenger.agent.lastIntent.toTravel = Some(transports.minBy(_.pixelDistanceFast(passenger)).pixelCenter)
        passenger.agent.lastIntent.canBerzerk = passenger.zone == destination.zone
      })
      convoys.foreach(convoy => {
        convoy.agent.lastIntent.toTravel = Some(keyTransport.pixelCenter.project(destination, 32.0 * 3.0))
      })
    }
  }
  
  override def acceptsHelp              : Boolean = true
  override def shouldRequireAntiAir     : Boolean = false
  override def shouldRequireAntiGround  : Boolean = false
  override def shouldRequireAirToAir    : Boolean = squad.enemies.exists(_.flying)
  override def shouldRequireAirToGround : Boolean = true
  override def shouldRequireTransport   : Boolean = true
}
