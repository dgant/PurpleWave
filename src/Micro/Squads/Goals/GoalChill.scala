package Micro.Squads.Goals

import Micro.Agency.Intention

class GoalChill extends GoalBasic {
  
  override def run() {
    squad.units.foreach(_.agent.intend(squad.client, new Intention))
  }
}
