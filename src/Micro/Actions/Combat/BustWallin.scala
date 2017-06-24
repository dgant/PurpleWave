package Micro.Actions.Combat

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Reposition
import Micro.Behaviors.MovementProfiles
import Micro.Execution.ActionState
import ProxyBwapi.Races.Terran
import bwapi.Race

object BustWallin extends Action {
  
  // Wall-ins mess with default behavior:
  // * Zealots try to hit units on the other side.
  // * Dragoons refuse to walk up the ramp.
  // * Dragoons are much better when trying to shoot from uphill
  //
  // So let's equip our units to fight vs. wall-ins.
  
  override protected def allowed(state: ActionState): Boolean = {
    val walledInBase = state.toTravel.flatMap(_.zone.bases.find(_.walledIn))
  
    walledInBase.isDefined                                  &&
    With.enemies.exists(_.race == Race.Terran)              &&
    state.canFight                                          &&
    state.unit.canMoveThisFrame                             &&
    walledInBase.get.zone.exit.exists(_.centerPixel.pixelDistanceFast(state.unit.pixelCenter) < 32.0 * 8.0) &&
    state.threats.forall(threat =>
      state.unit.inRangeToAttackFast(threat)
      || ! threat.is(Terran.SiegeTankSieged))
  }
  
  override protected def perform(state: ActionState) {
    Potshot.delegate(state)
    
    // Don't rely on BW's pathing to bring us into the wall-in.
    // Wall-ins tend to cause Dragoons to do the "walk around the perimeter of the map" dance
    state.movementProfile = MovementProfiles.smash
    Reposition.delegate(state)
  }
}
