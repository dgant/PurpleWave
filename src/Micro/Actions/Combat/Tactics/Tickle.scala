package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Basic.MineralWalk
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.{Attack, Move}
import Planning.UnitMatchers.UnitMatchWorkers
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Tickle extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.lastIntent.canTickle
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    // Potential improvements:
    // * Use convex hull to ensure we don't get trapped in the worker line
    // * Fight when injured if everyone nearby is targeting someone else
    // * Don't get pulled out of the base
    
    if ( ! unit.ready) return
    
    var attack                = true
  
    val zone                  = unit.agent.toTravel.get.zone
    val exit                  = zone.edges.map(_.pixelCenter).sortBy(_.groundPixels(With.geography.home)).headOption.getOrElse(With.geography.home.pixelCenter)
    val hurtThreshold         = 30
    val dyingThreshold        = 6
    val hurt                  = unit.totalHealth < hurtThreshold
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
    val centroidSoft  = PurpleMath.centroid(enemies.map(_.pixelCenter))
    val centroidHard  = if (enemies.size > 3) PurpleMath.centroid(enemies.sortBy(_.pixelDistanceCenter(centroidSoft)).take(enemies.size/2).map(_.pixelCenter)) else centroidSoft //Don't centroid an empty list
    
    // Get in their base!
    if ( ! unit.zone.bases.exists(_.owner.isEnemy)
      && ! enemies.exists(_.pixelDistanceEdge(unit) < 48.0)
      && enemiesAttackingUs.size < 1) {
      Move.consider(unit)
      return
    }
    
    // Never get surrounded
    if (
      zone.bases.exists(_.harvestingArea.contains(unit.tileIncludingCenter)
      && enemies.exists(enemy =>
        enemy.zone == zone
        && enemy.pixelDistanceEdge(unit) < 64.0
        && enemy.pixelDistanceCenter(exit) < unit.pixelDistanceCenter(exit)))) {
      mineralWalkAway(unit)
      return
    }
    
    // Try to avoid dying. Let our shield recharge work for us.
    if (dying) {
      attack = false
    }
    
    // Don't take losing fights
    if (enemiesAttackingUs.map(_.totalHealth).sum > unit.totalHealth) {
      attack = false
    }
    
    // If violent enemies completely overpower us, let's back off
    if (ourStrength < enemyFighterStrength && hurt) {
      attack = false
    }
  
    // Wait for re-enforcements
    val workersTotal  = With.units.countOurs(UnitMatchWorkers)
    val workersHere   = With.units.countOursP(u => u.unitClass.isWorker && u.zone == zone)
    if (workersHere * 2 < workersTotal  // Tomorrow, there'll be more of us.
      && enemies.size > 4) {            // Verus 4-Pool need to start dealing damage immediately
      attack = false
    }
    
    // Stay close to the fight
    if (unit.matchups.threats.forall(_.pixelDistanceEdge(unit) > 64)) {
      attack = true
    }
    
    // Heal up before going back in
    if (dying) {
      attack = false
    }
  
    // If we completely overpower the enemy, let's go kill 'em.
    val weOverpower = ourStrength > enemyStrength
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
          .sortBy(_.remainingCompletionFrames)
          .sortBy(_.totalHealth)
          .headOption
        Attack.delegate(unit)
      }
      
      // Ignore units outside their bases
      val targets = unit.matchups.targets.filter(target =>
        (
          target.zone == zone   ||
          // ...unless they're fighting us! Or building a building.
          target.constructing               ||
          unit.inRangeToAttack(target)  ||
          (
            target.isBeingViolent &&
            target.pixelDistanceTravelling(zone.centroid) <= unit.pixelDistanceTravelling(zone.centroid)
          )
        ) &&
        target.canAttack &&
        (
          // Don't get distracted by workers leaving the base
          target.targetPixel.forall(_.zone == zone) ||
          unit.inRangeToAttack(target)          ||
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
          targets.map(target => unit.framesToTravelPixels(unit.pixelDistanceEdge(target))).min) {
        // Let's pick the outermost target while avoiding drilling stacks
        val nearestTargetDistance = targets.map(_.pixelDistanceCenter(exit)).min
        val validTargets = targets.filter(_.pixelDistanceCenter(exit) - 16.0 <= nearestTargetDistance)
        val bestTarget =
          validTargets
            .sortBy(target => target.totalHealth * target.pixelDistanceEdge(unit))
            .headOption
            .getOrElse(targets.minBy(_.pixelDistanceCenter(exit)))
  
        unit.agent.toAttack = Some(bestTarget)
        Attack.consider(unit)
        
        return
      }
    }
    
    // SCVs shouldn't kite
    if (unit.is(Terran.SCV)) {
      return
    }
    
    // We're not attacking, so let's hang out and wait for opportunities
    if (enemiesAttackingUs.nonEmpty || enemies.exists(_.pixelDistanceEdge(unit) < 64.0)) {
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
            && unit.pixelDistanceEdge(defender) >
            unit.pixelDistanceEdge(freebie)))
      unit.agent.toAttack = freebies
      Attack.consider(unit)
      Avoid.consider(unit)
    }
  }
  
  private def mineralWalkAway(unit: FriendlyUnitInfo) {
    unit.agent.toGather = With.geography.ourBases.flatMap(_.minerals).headOption
    MineralWalk.consider(unit)
    unit.agent.toTravel = Some(unit.agent.origin)
    Move.consider(unit)
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
