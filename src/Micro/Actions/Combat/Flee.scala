package Micro.Actions.Combat

import Information.Geography.Types.Zone
import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.{Reposition, Travel}
import Micro.Behaviors.MovementProfiles
import Micro.Task.ExecutionState

object Flee extends Action {
  
  override def allowed(state: ExecutionState) = {
    state.unit.canMoveThisFrame &&
    (
      (
        state.unit.unitClass.isWorker &&
        state.threatsActive.nonEmpty
      ) ||
      (
        state.threats.exists(threat => zoneQualifies(threat.pixelCenter.zone)) &&
        zoneQualifies(state.unit.pixelCenter.zone)
      )
    )
  }
  
  private def zoneQualifies(zone: Zone): Boolean = {
    zone.owner != With.self || zone.bases.isEmpty
  }
  
  override def perform(state: ExecutionState) {
    
    state.movementProfile = MovementProfiles.flee
    
    state.canPursue = false
    state.toTravel  = Some(state.origin)
    val isBackLine = state.unit.battle.exists(battle =>
      battle.enemy.vanguard.pixelDistanceFast(state.unit.pixelCenter) >
      battle.enemy.vanguard.pixelDistanceFast(battle.us.centroid))
  
    if (isBackLine) {
      state.movementProfile = MovementProfiles.approach
      Reposition.delegate(state)
      if ( ! stillReady(state)) return
    }
  
    // If the enemy is faster, go straight home so we don't get caught
    val enemyFaster = state.threatsActive.exists(threat => threat.topSpeed > state.unit.topSpeed)
    if (enemyFaster) {
      Travel.delegate(state)
      if ( ! stillReady(state)) return
    }
  
    //If we're faster, we can be cuter with how we retreat
    val weAreFaster = state.threatsActive.forall(threat => threat.topSpeed < state.unit.topSpeed)
    if (weAreFaster) {
      Reposition.delegate(state)
      if ( ! stillReady(state)) return
    }
  
    // If we have a clear path home, then skip heuristic movement and just go.
    val ourDistanceToOrigin = state.unit.pixelDistanceTravelling(state.origin) - 32.0
    if (state.threatsActive.forall(threat =>
      ourDistanceToOrigin <= (
        if (state.unit.flying)  threat.pixelDistanceFast(state.origin) //Don't retreat directly over the enmy!
        else                    threat.pixelDistanceTravelling(state.origin)))) {
    
      Travel.delegate(state)
      if ( ! stillReady(state)) return
    }
  
    Reposition.delegate(state)
  }
}
