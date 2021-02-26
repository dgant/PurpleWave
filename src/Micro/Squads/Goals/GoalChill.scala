package Micro.Squads.Goals

import Micro.Agency.Intention

class GoalChill extends SquadGoal {
  override def run() {
    squad.units.foreach(_.agent.intend(this, new Intention))
  }
}
