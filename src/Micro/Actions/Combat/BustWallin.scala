package Micro.Actions.Combat

import Lifecycle.With
import Mathematics.Shapes.Circle
import Micro.Actions.Action
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Behaviors.MovementProfiles
import Micro.Execution.ActionState
import Micro.Heuristics.Targeting.EvaluateTargets
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
    val walledInZones = With.geography.enemyZones.filter(_.walledIn)
  
    walledInZones.nonEmpty                                  &&
    With.enemies.exists(_.race == Race.Terran)              &&
    state.canFight                                          &&
    state.unit.canMoveThisFrame                             &&
    walledInZones.flatMap(_.edges).exists(_.centerPixel.pixelDistanceFast(state.unit.pixelCenter) < 32.0 * 8.0) &&
    state.threats.forall(threat =>
      state.unit.inRangeToAttackFast(threat)
      || ! threat.is(Terran.SiegeTankSieged))
  }
  
  override protected def perform(state: ActionState) {
    Potshot.delegate(state)
    
    // Don't rely on BW's pathing to bring us into the wall-in.
    // Wall-ins tend to cause Dragoons to do the "walk around the perimeter of the map" dance
    state.movementProfile = MovementProfiles.smash
  
    val targets =
      if (state.targetsInRange.nonEmpty)
        state.targetsInRange
      else if (state.targets.nonEmpty)
        Iterable(state.targets.minBy(target => state.unit.pixelDistanceTravelling(target.pixelCenter)))
      else
        Iterable.empty
    
    state.toAttack = EvaluateTargets.best(state, targets)
    Attack.delegate(state)
    if (stillReady(state) && ! state.unit.melee && state.toAttack.isDefined) {
      // Get up in there!
      val targetUnit = state.toAttack.get
      val targetTile = targetUnit.tileIncludingCenter
      val targetAreaTiles = Circle.points(4).map(targetTile.add)
      val targetSpots = targetAreaTiles
        .filter(With.grids.walkable.get)
        .map(_.pixelCenter)
        .filter(pixel => state.unit.pixelDistanceFast(pixel) < state.unit.pixelDistanceFast(targetUnit))
      if (targetSpots.nonEmpty) {
        state.toTravel = Some(targetSpots.minBy(targetUnit.pixelDistanceFast))
        Travel.delegate(state)
      }
    }
  }
}
