package Micro.Squads.Goals

import Mathematics.Points.Pixel
import Micro.Agency.Intention

class SquadPush(pixel: Pixel) extends SquadGoal {
  
  override def toString: String = "Attack " + pixel.zone.name
  
  def updateUnits() {
    squad.recruits.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(pixel)
    }))
  }
  
  override def shouldRequireAntiGround : Boolean = true
  override def shouldRequireSiege      : Boolean = true
  override def shouldRequireSpotters   : Boolean = true
}
