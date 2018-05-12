package Micro.Squads.Goals

import Mathematics.Points.Pixel
import Micro.Agency.Intention

class GoalPush(var pixel: Pixel) extends GoalBasic {
  
  override def toString: String = "Attack " + pixel.zone.name
  
  override def run() {
    squad.recruits.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(pixel)
    }))
  }
}
