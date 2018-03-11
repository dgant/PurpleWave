package Micro.Squads.Goals

import Mathematics.Points.Pixel
import Micro.Agency.Intention

class SquadPush(pixel: Pixel) extends SquadGoal {
  
  var berzerk = false
  
  override def toString: String = "Attack " + pixel.zone.name
  
  def updateUnits() {
    squad.recruits.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(pixel)
      canBerzerk = berzerk
    }))
  }
  
  override def shouldRequireAntiGround : Boolean = true
  override def shouldRequireSiege      : Boolean = true
  override def shouldRequireSpotters   : Boolean = true
}
