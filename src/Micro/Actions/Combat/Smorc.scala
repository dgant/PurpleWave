package Micro.Actions.Combat

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Basic.MineralWalk
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Execution.ActionState
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel._

object Smorc extends Action {
  override protected def allowed(state: ActionState): Boolean = {
    state.intent.smorc
  }
  
  override protected def perform(state: ActionState) {
    
    // Potential improvements:
    // * Use convex hull to ensure we don't get trapped in the worker line
    // * Fight when injured if everyone nearby is targeting someone else
    // * Don't get pulled out of the base
    
    if ( ! stillReady(state)) return
    
    var attack                = true
  
    val zone                  = state.toTravel.get.zone
    val exit                  = zone.edges.map(_.centerPixel).sortBy(_.groundPixels(With.geography.home)).headOption.getOrElse(With.geography.home.pixelCenter)
    val dyingThreshold        = 11
    val dying                 = state.unit.totalHealth < dyingThreshold
    val enemies               = state.threats
    val enemyFighters         = state.threats.filter(_.isBeingViolent)
    val enemiesAttackingUs    = enemies.filter(_.isBeingViolentTo(state.unit))
    val allyFighters          = state.neighbors
    val allyFightersDying     = allyFighters.filter(_.totalHealth < dyingThreshold)
    val strength              = (units: Iterable[UnitInfo]) => units.size * units.map(_.totalHealth).sum
    val ourStrength           = strength(allyFighters :+ state.unit)
    val enemyStrength         = strength(enemies)
    val enemyFighterStrength  = strength(enemyFighters)
  
    // We want to avoid big drilling stacks
    val centroidSoft    = enemies.map(_.pixelCenter).centroid
    val centroidHard    = if (enemies.size > 3) enemies.sortBy(_.pixelDistanceFast(centroidSoft)).take(enemies.size/2).map(_.pixelCenter).centroid else centroidSoft //Don't centroid an empty list
    
    // Get in their base!
    if ( ! state.unit.pixelCenter.zone.bases.exists(_.owner.isEnemy) && enemiesAttackingUs.size <= 1) {
      Travel.consider(state)
      return
    }
    
    // Never get surrounded
    if (
      zone.bases.exists(_.harvestingArea.contains(state.unit.tileIncludingCenter)
      && enemies.exists(enemy =>
        enemy.pixelCenter.zone == zone
        && enemy.pixelDistanceFast(state.unit) < 64.0
        && enemy.pixelDistanceFast(exit) < state.unit.pixelDistanceFast(exit)))) {
      mineralWalkAway(state)
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
      val staticDefense = state.targets.filter(u => u.unitClass.isBuilding && u.unitClass.canAttack)
      if (staticDefense.nonEmpty) {
        state.toAttack = staticDefense
          .sortBy(_.remainingBuildFrames)
          .sortBy(_.totalHealth)
          .headOption
        Attack.delegate(state)
      }
      
      // Ignore units outside their bases
      // TODO: If they're pushing us out of their base we should fight back
      val targets = state.targets.filter(unit =>
        unit.pixelCenter.zone == zone &&
        unit.canAttackThisSecond      &&
        (
          // Don't get distracted by workers leaving the base
          unit.targetPixel.forall(_.zone == zone) ||
          state.unit.inRangeToAttackFast(unit)
        ))
      if (targets.isEmpty) {
        destroyBuildings(state)
        return
      }
      // Don't waste time mineral walking -- we need to get off as many shots as possible
      else if (
        state.unit.canAttackThisFrame ||
        state.unit.cooldownLeft - With.latency.framesRemaining <=
          targets.map(target =>state.unit.framesToTravelPixels(state.unit.pixelsFromEdgeFast(target))).min) {
        // Let's pick the outermost target while avoiding drilling stacks
        val nearestTargetDistance = targets.map(_.pixelDistanceFast(exit)).min
        val validTargets = targets.filter(_.pixelDistanceFast(exit) - 16.0 <= nearestTargetDistance)
        val bestTarget =
          validTargets
            .sortBy(target => target.totalHealth * target.pixelDistanceFast(state.unit))
            .headOption
            .getOrElse(targets.minBy(_.pixelDistanceFast(exit)))
  
        state.toAttack = Some(bestTarget)
        Attack.consider(state)
        
        return
      }
    }
      
    // We're not attacking, so let's hang out and wait for opportunities
    if (enemiesAttackingUs.nonEmpty || enemies.exists(_.pixelDistanceFast(state.unit) < 64.0)) {
      // Extra aggression vs. 4-pool
      if ( ! weOverpower) {
        mineralWalkAway(state)
      }
    }
    else {
      val freebies = enemies.find(freebie =>
        (freebie.unitClass.isBuilding || freebie.constructing)
          && freebie.armorHealth < 5
          && enemies.forall(defender =>
          defender != freebie
            && state.unit.pixelDistanceFast(defender) >
            state.unit.pixelDistanceFast(freebie)))
      state.toAttack = freebies
      Attack.consider(state)
      HoverOutsideRange.consider(state)
    }
  }
  
  private def mineralWalkAway(state: ActionState) {
    state.toGather = With.geography.ourBases.flatMap(_.minerals).headOption
    MineralWalk.consider(state)
    state.toTravel = Some(state.origin)
    Travel.consider(state)
  }
  
  private def destroyBuildings(state: ActionState) {
    // Vs. 4-pool they will often be left with just an egg or two.
    // We need to surround it and do as much damage to it as possible
    val egg = With.units.enemy.filter(_.is(Zerg.Egg)).toVector.sortBy(_.totalHealth).headOption
    lazy val nonEgg = state.targets.sortBy(_.totalHealth).headOption
    state.toAttack = egg.orElse(nonEgg)
    Attack.consider(state)
  }
}
