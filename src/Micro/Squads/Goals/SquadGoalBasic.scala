package Micro.Squads.Goals

import Micro.Agency.Intention
import Micro.Squads.Recruitment.SquadRecruiterSimple

trait SquadGoalBasic extends SquadGoal with SquadRecruiterSimple with SettableSquad {
  def run() {
    squad.units.foreach(_.agent.intend(this, new Intention { toTravel = Some(destination) }))
  }
}