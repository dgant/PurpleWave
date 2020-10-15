package Micro.Actions.Combat.Tactics

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Information.Fingerprinting.Generic.GameTime
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Targeting.Filters.TargetFilterWhitelist
import Micro.Actions.Combat.Targeting.TargetAction
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption
import bwapi.Race

object Batter extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    (
      With.frame < GameTime(7, 0)()
      && unit.canMove
      && unit.canAttack
      && ! unit.flying
      && With.enemies.exists(_.raceInitial == Race.Terran)
      && ! unit.matchups.threatsInRange.exists(_.is(Terran.SiegeTankSieged))
      && walledZone(unit).exists(_.exit.exists(exit => unit.pixelDistanceCenter(exit.pixelCenter) < 32.0 * 12.0))
    )
  }
  
  private def destinationZone(unit: FriendlyUnitInfo): Zone = unit.agent.destination.zone
  
  private def walledZone(unit: FriendlyUnitInfo): Option[Zone] =
    if (destinationZone(unit).walledIn)
      Some(destinationZone(unit))
    else
      With.paths.zonePath(unit.zone, unit.agent.destination.zone).flatMap(_.steps.map(_.to).find(_.walledIn))
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    lazy val wallZone  = walledZone(unit).get
    lazy val wallExit  = wallZone.exit.get
    lazy val wallUnits = wallZone.units
      .filter(u =>
        u.isEnemy
        && ! u.flying
        && u.unitClass.isBuilding
        && u.pixelDistanceCenter(wallExit.pixelCenter) < 32.0 * 10.0)
      .sortBy(_.pixelDistanceCenter(wallExit.pixelCenter))
  
    lazy val outsideUnits   = unit.matchups.targetsInRange
    lazy val repairingUnits = wallUnits.flatMap(_.matchups.repairers)
    lazy val altitudeHere   = With.grids.altitudeBonus.get(unit.tileIncludingCenter)
    lazy val altitudeThere  = With.grids.altitudeBonus.get(unit.agent.destination.tileIncluding)
    
    lazy val legalTargetOutsiders = outsideUnits.filter(_.visible)
    lazy val legalTargetRepairers = if (unit.pixelRangeGround >= 32.0 * 4.0) repairingUnits.filter(_.visible) else Iterable.empty
    lazy val legalTargetWall      = wallUnits.filter(_.visible)
    
    lazy val targetOutsiders  = new TargetAction(TargetFilterWhitelist(legalTargetOutsiders))
    lazy val targetRepairers  = new TargetAction(TargetFilterWhitelist(legalTargetRepairers))
    lazy val targetWall       = new TargetAction(TargetFilterWhitelist(legalTargetWall))
    
    lazy val shootingThreats  = unit.matchups.framesOfEntanglementPerThreat.filter(_._2 > - GameTime(0, 1)())
    lazy val dpfReceiving     = shootingThreats.map(_._1.dpfOnNextHitAgainst(unit)).sum
    lazy val framesToLive     = PurpleMath.nanToInfinity(unit.totalHealth / dpfReceiving)
    lazy val dying            = framesToLive < GameTime(0, 1)()
    lazy val shouldRetreat    = ! unit.agent.shouldEngage || (unit.unitClass.melee && dying)
    
    if (shouldRetreat) {
      Retreat.consider(unit)
    }
  
    if (unit.ready) {
      targetOutsiders.delegate(unit)
      targetRepairers.delegate(unit)
      targetWall.delegate(unit)
    }
    
    // TODO: Walk up to the wall when our mobility > 1 and we're not shooting.
    
    if (unit.readyForAttackOrder || unit.agent.toAttack.forall(unit.inRangeToAttack)) {
      Attack.delegate(unit)
    }
  
    if (unit.ready && altitudeHere < altitudeThere) {
      val walkableTiles = wallUnits
        .flatMap(building =>
          building.tileArea.expand(1, 1).tiles
            .filter(With.grids.walkable.get)
            .filter(
              _.pixelCenter.pixelDistance(wallExit.pixelCenter)
              < building.pixelCenter.pixelDistance(wallExit.pixelCenter)))
        .distinct
      
      val destination = ByOption.minBy(walkableTiles)(_.pixelCenter.pixelDistance(unit.pixelCenter))
      unit.agent.toTravel = destination.map(_.pixelCenter).orElse(unit.agent.toTravel)
      Move.delegate(unit)
  
      //TMP
      walkableTiles.foreach(tile => DrawMap.circle(tile.pixelCenter, 15, Colors.DarkYellow))
      destination.foreach(tile => DrawMap.circle(tile.pixelCenter, 12, Colors.MediumYellow))
    }
  }
}
