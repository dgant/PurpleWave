package Micro.Actions.Combat.Techniques

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Information.Geography.Types.Zone
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Combat.Attacking.Filters.TargetFilterWhitelist
import Micro.Actions.Combat.Attacking.TargetAction
import Micro.Actions.Combat.Decisionmaking.Leave
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption
import bwapi.Race

object Batter extends ActionTechnique {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    (
      unit.canMove
      && unit.canAttack
      && ! unit.flying
      && With.enemies.exists(_.raceInitial == Race.Terran)
      && ! unit.matchups.threatsInRange.exists(_.is(Terran.SiegeTankSieged))
      && walledZone(unit).isDefined
      && walledZone(unit).get.exit.exists(exit => unit.pixelDistanceCenter(exit.pixelCenter) < 32.0 * 12.0)
    )
  }
  
  private def destinationZone(unit: FriendlyUnitInfo): Zone = unit.agent.destination.zone
  
  private def walledZone(unit: FriendlyUnitInfo): Option[Zone] =
    if (destinationZone(unit).walledIn)
      Some(destinationZone(unit))
    else
      unit.agent
        .zonePath(unit.agent.destination)
        .flatMap(_.steps.map(_.to).find(_.walledIn))
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    lazy val wallZone  = walledZone(unit).get
    lazy val wallExit  = wallZone.exit.get
    lazy val wallUnits = wallZone.units
      .filter(u =>
        u.isEnemy
        && ! u.flying
        && u.unitClass.isBuilding
        && u.pixelDistanceCenter(wallExit.pixelCenter) < 32.0 * 10.0)
      .toSeq
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
    
    lazy val shootingThreats  = unit.matchups.framesOfEntanglementPerThreatDiffused.filter(_._2 > - GameTime(0, 1)())
    lazy val dpfReceiving     = shootingThreats.map(_._1.dpfOnNextHitAgainst(unit)).sum
    lazy val framesToLive     = PurpleMath.nanToInfinity(unit.totalHealth / dpfReceiving)
    lazy val dying            = framesToLive < GameTime(0, 1)()
    lazy val shouldRetreat    = ! unit.agent.shouldEngage || (unit.unitClass.melee && dying)
    
    if (shouldRetreat) {
      Leave.consider(unit)
    }
  
    if (unit.readyForMicro) {
      targetOutsiders.delegate(unit)
      targetRepairers.delegate(unit)
      targetWall.delegate(unit)
    }
    
    // TODO: Walk up to the wall when our mobility > 1 and we're not shooting.
    
    if (unit.readyForAttackOrder || unit.agent.toAttack.forall(unit.inRangeToAttack)) {
      Attack.delegate(unit)
    }
  
    if (unit.readyForMicro && altitudeHere < altitudeThere) {
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
