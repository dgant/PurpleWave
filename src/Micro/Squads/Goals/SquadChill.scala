package Micro.Squads.Goals

import Micro.Agency.Intention
import Micro.Squads.Squad

object SquadChill extends SquadGoal {
  
  def update(squad: Squad) {
    squad.recruits.foreach(_.agent.intend(squad.client, new Intention))
  }
}
