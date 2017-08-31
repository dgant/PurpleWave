package Micro.Squads.Goals

import Micro.Agency.Intention

class SquadChill extends SquadGoal {
  
  def updateUnits() {
    squad.recruits.foreach(_.agent.intend(squad.client, new Intention))
  }
}
