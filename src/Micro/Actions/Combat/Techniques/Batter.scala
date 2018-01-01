package Micro.Actions.Combat.Techniques

import Lifecycle.With
import Mathematics.Shapes.Circle
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, Leave}
import Micro.Actions.Commands.{Attack, Move}
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import bwapi.Race

object Batter extends ActionTechnique {
  
  // Wall-ins mess with default behavior:
  // * Zealots try to hit units on the other side.
  // * Dragoons refuse to walk up the ramp.
  // * Dragoons are much better when trying to shoot from uphill
  //
  // So let's equip our units to fight vs. wall-ins.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    
    lazy val walledInZones = With.geography.zones.filter(_.walledIn)
  
    (
      unit.canMove
      && unit.canAttack
      && ! unit.flying
      && walledInZones.nonEmpty
      && With.enemies.exists(_.raceInitial == Race.Terran)
      && walledInZones.flatMap(_.edges).exists(_.centerPixel.pixelDistanceFast(unit.pixelCenter) < 32.0 * 8.0)
      && ! unit.matchups.threatsInRange.exists(_.is(Terran.SiegeTankSieged))
    )
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    // TODO: Upgrade all this
    
    Potshot.delegate(unit)
    
    // Back wounded units off
    val escapeFrames    = 2.0 * Math.max(0, unit.matchups.framesOfEntanglementDiffused) + 24 + 2 * With.reaction.agencyAverage
    val damagePerFrame  = unit.matchups.threatsInRange.map(_.dpfOnNextHitAgainst(unit)).sum
    if (unit.totalHealth < damagePerFrame * escapeFrames) {
      Leave.consider(unit)
    }
  
    val targets =
      if (unit.matchups.targetsInRange.nonEmpty)
        unit.matchups.targetsInRange
      else if (unit.matchups.targets.nonEmpty)
        Iterable(unit.matchups.targets.minBy(target => unit.pixelDistanceTravelling(target.pixelCenter)))
      else
        Iterable.empty
    
    unit.agent.toAttack = EvaluateTargets.best(unit, targets)
    
    if (unit.readyForAttackOrder || unit.unitClass.melee) {
      Attack.delegate(unit)
    }
    else if (unit.agent.toAttack.isDefined) {
      // Get up in there!
      val targetUnit = unit.agent.toAttack.get
      val targetTile = targetUnit.tileIncludingCenter
      val targetAreaTiles = Circle.points(4).map(targetTile.add)
      val targetSpots = targetAreaTiles
        .filter(With.grids.walkable.get)
        .map(_.pixelCenter)
        .filter(pixel => unit.pixelDistanceFast(pixel) < unit.pixelDistanceFast(targetUnit))
      if (targetSpots.nonEmpty) {
        unit.agent.toTravel = Some(targetSpots.minBy(targetUnit.pixelDistanceFast))
        Move.delegate(unit)
      }
    }
  }
}
