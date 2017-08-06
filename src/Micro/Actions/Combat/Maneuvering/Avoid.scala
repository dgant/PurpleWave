package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Commands.MoveHeuristically
import Micro.Agency.MovementProfiles
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Avoid extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    /*
    There are multiple different ways to avoid someone:
    
    1. Move directly away (which is great until you hit a wall)
    2. Move to the exit closest to home (best in the long term, but you may die along the way)
    3. Move to the nearest exit (good in the medium term; bad when you're initially getting hit and bad long-term if you're outsped)
    4. Move to help (make chasers run into a bunch of Siege Tanks, for example)
    
    So we want to pick the appropriate getaway technique based on the situation.
    */
  
    lazy val asymptoticallySafe = unit.matchups.threats.forall(_.topSpeedChasing < unit.topSpeed)
    lazy val zone               = unit.pixelCenter.zone
    lazy val exits              = zone.edges.map(_.centerPixel)
    lazy val threat             = unit.matchups.mostEntangledThreatDiffused
    
    
    
    
    unit.agent.movementProfile = MovementProfiles.avoid
    MoveHeuristically.delegate(unit)
  }
}
