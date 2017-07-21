package Micro.Actions.Combat

import Lifecycle.With
import Mathematics.Shapes.Circle
import Micro.Actions.Action
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Behaviors.MovementProfiles
import Micro.Execution.ActionState
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import bwapi.Race

object BustWallin extends Action {
  
  // Wall-ins mess with default behavior:
  // * Zealots try to hit units on the other side.
  // * Dragoons refuse to walk up the ramp.
  // * Dragoons are much better when trying to shoot from uphill
  //
  // So let's equip our units to fight vs. wall-ins.
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    val walledInZones = With.geography.zones.filter(_.walledIn)
  
    walledInZones.nonEmpty                            &&
    With.enemies.exists(_.race == Race.Terran)        &&
    unit.action.canFight                              &&
    unit.canMoveThisFrame                             &&
    walledInZones.flatMap(_.edges).exists(_.centerPixel.pixelDistanceFast(unit.pixelCenter) < 32.0 * 8.0) &&
    unit.matchups.threats.forall(threat =>
      unit.inRangeToAttackFast(threat)
      || ! threat.is(Terran.SiegeTankSieged))
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.delegate(unit)
    
    // Don't rely on BW's pathing to bring us into the wall-in.
    // Wall-ins tend to cause Dragoons to do the "walk around the perimeter of the map" dance
    unit.action.movementProfile = MovementProfiles.smash
  
    val targets =
      if (unit.matchups.targetsInRange.nonEmpty)
        unit.matchups.targetsInRange
      else if (unit.matchups.targets.nonEmpty)
        Iterable(unit.matchups.targets.minBy(target => unit.pixelDistanceTravelling(target.pixelCenter)))
      else
        Iterable.empty
    
    unit.action.toAttack = EvaluateTargets.best(unit.action, targets)
    
    if (unit.readyForAttackOrder || unit.melee) {
      Attack.delegate(unit)
    }
    else if (unit.action.toAttack.isDefined) {
      // Get up in there!
      val targetUnit = unit.action.toAttack.get
      val targetTile = targetUnit.tileIncludingCenter
      val targetAreaTiles = Circle.points(4).map(targetTile.add)
      val targetSpots = targetAreaTiles
        .filter(With.grids.walkable.get)
        .map(_.pixelCenter)
        .filter(pixel => unit.pixelDistanceFast(pixel) < unit.pixelDistanceFast(targetUnit))
      if (targetSpots.nonEmpty) {
        unit.action.toTravel = Some(targetSpots.minBy(targetUnit.pixelDistanceFast))
        Travel.delegate(unit)
      }
    }
  }
}
