package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Basic.MineralWalk
import Micro.Actions.Combat.Maneuvering.Avoid
import Micro.Actions.Commands.{Attack, Travel}
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.EnrichPixel._

object Tickle extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.lastIntent.canTickle
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    // Potential improvements:
    // * Use convex hull to ensure we don't get trapped in the worker line
    // * Fight when injured if everyone nearby is targeting someone else
    // * Don't get pulled out of the base
    
    if ( ! unit.readyForMicro) return
    
    var attack                = true
  
    val zone                  = unit.agent.toTravel.get.zone
    val exit                  = zone.edges.map(_.centerPixel).sortBy(_.groundPixels(With.geography.home)).headOption.getOrElse(With.geography.home.pixelCenter)
    val dyingThreshold        = 11
    val dying                 = unit.totalHealth < dyingThreshold
    val enemies               = unit.matchups.threats
    val enemyFighters         = unit.matchups.threats.filter(_.isBeingViolent)
    val enemiesAttackingUs    = enemies.filter(_.isBeingViolentTo(unit))
    val allyFighters          = unit.matchups.allies
    val strength              = (units: Iterable[UnitInfo]) => units.size * units.map(_.totalHealth).sum
    val ourStrength           = strength(allyFighters :+ unit)
    val enemyStrength         = strength(enemies)
    val enemyFighterStrength  = strength(enemyFighters)
  
    // We want to avoid big drilling stacks
    val centroidSoft    = enemies.map(_.pixelCenter).centroid
    val centroidHard    = if (enemies.size > 3) enemies.sortBy(_.pixelDistanceFast(centroidSoft)).take(enemies.size/2).map(_.pixelCenter).centroid else centroidSoft //Don't centroid an empty list
    
    // Get in their base!
    if ( ! unit.pixelCenter.zone.bases.exists(_.owner.isEnemy)
      && ! enemies.exists(_.pixelDistanceFast(unit) < 48.0)
      && enemiesAttackingUs.size < 1) {
      Travel.consider(unit)
      return
    }
    
    // Never get surrounded
    if (
      zone.bases.exists(_.harvestingArea.contains(unit.tileIncludingCenter)
      && enemies.exists(enemy =>
        enemy.pixelCenter.zone == zone
        && enemy.pixelDistanceFast(unit) < 64.0
        && enemy.pixelDistanceFast(exit) < unit.pixelDistanceFast(exit)))) {
      mineralWalkAway(unit)
      return
    }
    
    // Try to avoid dying. Let our shield recharge work for us.
    if (dying) {
      attack = false
    }
    
    // Don't take losing fights
    if (enemiesAttackingUs.size > 1) {
      attack = false
    }
    
    // If violent enemies completely overpower us, let's back off
    if (ourStrength < enemyFighterStrength) {
      attack = false
    }
  
    // Wait for re-enforcements
    val workersTotal  = With.units.ours.count(u => u.unitClass.isWorker)
    val workersHere   = With.units.ours.count(u => u.unitClass.isWorker && u.pixelCenter.zone == zone)
    if (workersHere * 2 < workersTotal  // Tomorrow, there'll be more of us.
      && enemies.size > 4               // Verus 4-Pool need to start dealing damage immediately
      ) {
      attack = false
    }
  
    // If we completely overpower the enemy, let's go kill 'em.
    var weOverpower = ourStrength > enemyStrength
    if (weOverpower) {
      attack = true
    }
    
    // Lastly, if they've started training combat units, we are ALL IN
    if (enemies.exists( ! _.unitClass.isWorker)) {
      attack = true
    }
  
    if (attack) {
      // No static defense allowed!
      val staticDefense = unit.matchups.targets.filter(u => u.unitClass.isBuilding && u.unitClass.rawCanAttack)
      if (staticDefense.nonEmpty) {
        unit.agent.toAttack = staticDefense
          .sortBy(_.remainingBuildFrames)
          .sortBy(_.totalHealth)
          .headOption
        Attack.delegate(unit)
      }
      
      // Ignore units outside their bases
      val targets = unit.matchups.targets.filter(target =>
        (
          target.pixelCenter.zone == zone   ||
          // ...unless they're fighting us! Or building a building.
          target.constructing               ||
          unit.inRangeToAttackFast(target)  ||
          (
            target.isBeingViolent &&
            target.pixelDistanceTravelling(zone.centroid) <= unit.pixelDistanceTravelling(zone.centroid)
          )
        ) &&
        target.canAttack &&
        (
          // Don't get distracted by workers leaving the base
          target.targetPixel.forall(_.zone == zone) ||
          unit.inRangeToAttackFast(target)          ||
          target.isBeingViolent                     ||
          unit.constructing
        ))
      if (targets.isEmpty) {
        destroyBuildings(unit)
        return
      }
      // Don't waste time mineral walking -- we need to get off as many shots as possible
      else if (
        unit.readyForAttackOrder ||
        unit.cooldownLeft - With.latency.framesRemaining <=
          targets.map(target => unit.framesToTravelPixels(unit.pixelsFromEdgeFast(target))).min) {
        // Let's pick the outermost target while avoiding drilling stacks
        val nearestTargetDistance = targets.map(_.pixelDistanceFast(exit)).min
        val validTargets = targets.filter(_.pixelDistanceFast(exit) - 16.0 <= nearestTargetDistance)
        val bestTarget =
          validTargets
            .sortBy(target => target.totalHealth * target.pixelDistanceFast(unit))
            .headOption
            .getOrElse(targets.minBy(_.pixelDistanceFast(exit)))
  
        unit.agent.toAttack = Some(bestTarget)
        Attack.consider(unit)
        
        return
      }
    }
      
    // We're not attacking, so let's hang out and wait for opportunities
    if (enemiesAttackingUs.nonEmpty || enemies.exists(_.pixelDistanceFast(unit) < 64.0)) {
      // Extra aggression vs. 4-pool
      if ( ! weOverpower) {
        mineralWalkAway(unit)
      }
    }
    else {
      val freebies = enemies.find(freebie =>
        (freebie.unitClass.isBuilding || freebie.constructing)
          && freebie.armorHealth < 5
          && enemies.forall(defender =>
          defender != freebie
            && unit.pixelDistanceFast(defender) >
            unit.pixelDistanceFast(freebie)))
      unit.agent.toAttack = freebies
      Attack.consider(unit)
      Avoid.consider(unit)
    }
  }
  
  private def mineralWalkAway(unit: FriendlyUnitInfo) {
    unit.agent.toGather = With.geography.ourBases.flatMap(_.minerals).headOption
    MineralWalk.consider(unit)
    unit.agent.toTravel = Some(unit.agent.origin)
    Travel.consider(unit)
  }
  
  private def destroyBuildings(unit: FriendlyUnitInfo) {
    // Vs. 4-pool they will often be left with just an egg or two.
    // We need to surround it and do as much damage to it as possible
    val egg = With.units.enemy.filter(_.is(Zerg.Egg)).toVector.sortBy(_.totalHealth).headOption
    lazy val nonEgg = With.units.enemy.filter(_.unitClass.isBuilding).toVector.sortBy(_.totalHealth).headOption
    unit.agent.toAttack = egg.orElse(nonEgg)
    Attack.consider(unit)
  }
}
