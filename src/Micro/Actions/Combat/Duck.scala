package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Reposition
import Micro.Execution.{ActionState, Explosion}
import ProxyBwapi.Races.{Protoss, Terran}

import scala.collection.mutable.ListBuffer

object Duck extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    
    state.unit.canMoveThisFrame
  }
  
  override protected def perform(state: ActionState) {
    state.explosions ++= getDodgables(state)
    
    if (state.explosions.exists(explosion =>
      explosion.radius >=
      explosion.center.pixelDistanceFast(state.unit.pixelCenter) + 32.0)) {
      
      Reposition.delegate(state)
    }
  }
  
  private def getDodgables(state: ActionState): Iterable[Explosion] = {
    val output = new ListBuffer[Explosion]
    
    if ( ! state.unit.flying) {
      output ++= state.threats
        .filter(threat =>
          threat.target.isDefined               &&
          ! threat.target.contains(state.unit)  &&
          (
            threat.is(Protoss.Scarab)     ||
            threat.is(Terran.SpiderMine)
          ))
        .map(threat => Explosion(
          threat.target.get.pixelCenter,
          32.0 * 2.0,
          threat.damageAgainst(state.unit)))
    }
    
    output
  }
}
