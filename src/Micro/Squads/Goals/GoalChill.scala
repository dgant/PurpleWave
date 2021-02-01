package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention

class GoalChill extends SquadGoalBasic {
  
  override def run() {
    squad.units.foreach(_.agent.intend(this, new Intention))
  }

  override def destination: Pixel = With.geography.home.pixelCenter
}
