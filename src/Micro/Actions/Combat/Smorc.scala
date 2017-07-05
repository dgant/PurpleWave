package Micro.Actions.Combat

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Basic.MineralWalk
import Micro.Actions.Commands.Attack
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

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
    val exit                  = zone.exit.map(_.centerPixel).getOrElse(With.geography.home.pixelCenter)
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
    
    // Never get surrounded
    if (
      zone.bases.exists(_.harvestingArea.contains(state.unit.tileIncludingCenter)
      && enemies.exists(enemy =>
         enemy.pixelCenter.zone == zone
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
  
    // If we completely overpower the enemy, let's go kill 'em.
    if (ourStrength > enemyStrength) {
      attack = true
    }
    
    // If violent enemies completely overpower us, let's back off
    if (ourStrength < enemyFighterStrength) {
      attack = false
    }
  
    if (attack) {
      // Ignore units outside their bases
      // TODO: If they're pushing us out of their base we should fight back
      val targets = With.units.enemy.filter(unit => unit.pixelCenter.zone == zone && unit.canAttackThisSecond)
      if (targets.isEmpty) {
        destroyBuildings(state)
        return
      }
      else if (state.unit.canAttackThisFrame) {
        val nearestTargetDistance = targets.map(_.pixelDistanceFast(exit)).min
        val validTargets = targets.filter(target => target.pixelDistanceFast(exit) - 16.0 <= nearestTargetDistance)
        state.toAttack = Some(validTargets
          .toVector
          .sortBy(target => target.totalHealth * target.pixelDistanceFast(state.unit))
          .headOption
          .getOrElse(targets.minBy(_.pixelDistanceFast(exit))))
  
        Attack.consider(state)
        return
      }
    }
      
    // We're not attacking, so let's hang out and wait for opportunities
    if (enemiesAttackingUs.nonEmpty) {
      mineralWalkAway(state)
    } else {
      HoverOutsideRange.consider(state)
    }
  }
  
  private def mineralWalkAway(state: ActionState) {
    state.toGather = With.geography.ourBases.flatMap(_.minerals).headOption
    MineralWalk.consider(state)
    Retreat.consider(state)
  }
  
  private def destroyBuildings(state: ActionState) {
    state.toAttack = With.units.enemy.toList.sortBy(_.pixelDistanceFast(state.unit)).headOption
    Attack.consider(state)
  }
}
