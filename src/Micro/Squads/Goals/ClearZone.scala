package Micro.Squads.Goals

import Information.Geography.Types.Zone
import Micro.Agency.Intention
import Micro.Squads.Squad

class ClearZone(zone: Zone) extends SquadGoal {
  
  def update(squad: Squad) {
    squad.recruits.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(zone.centroid.pixelCenter)
    }))
  }
  
}
