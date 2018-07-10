package Micro.Squads.Goals

import Mathematics.Points.Pixel
import Micro.Agency.Intention

class GoalSmash(var pixel: Pixel) extends GoalBasic {
  
  override def toString: String = "Smash " + pixel.zone.name
  
  override def run() {
    squad.units.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(pixel)
      canFlee = false
    }))
  }
}
