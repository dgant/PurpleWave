package Micro.Actions.Combat

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Execution.ActionState

object Smorc extends Action {
  override protected def allowed(state: ActionState): Boolean = {
    state.intent.smorc
  }
  
  override protected def perform(state: ActionState) {
    
    /*
    How to SMORC:
    * If there's an SCV building something, send someone to SMORC it
    * Otherwise, kite the worker closest to the exit
     */
    
    // Do not throw away your shot.
    Potshot.delegate(state)
    if ( ! stillReady(state)) return
    
    var attack            = true
    val dyingThreshold    = 11
    val dying             = state.unit.totalHealth < dyingThreshold
    val enemies           = state.threats
    val enemyFighters     = state.threats.filter(_.isBeingViolent)
    val allyFighters      = state.neighbors
    val allyFightersDying = allyFighters.filter(_.totalHealth < dyingThreshold)
    
    // Try to avoid dying and let our shield recharge work for us.
    if (dying) {
      attack = false
    }
    
    // If we completely overpower the enemy, let's go kill 'em.
    if (allyFighters.size * allyFighters.map(_.totalHealth).sum > enemies.size * enemies.map(_.totalHealth).sum) {
      attack = true
    }
  
    val zone = state.toTravel.get.zone
    val exit = zone.exit.map(_.centerPixel).getOrElse(With.geography.home.pixelCenter)
    if (attack) {
      // Ignore units outside their bases
      // TODO: If they're pushing us out of their base we should fight back
      val targets = With.units.enemy.filter(unit => unit.pixelCenter.zone == zone && unit.canAttackThisSecond)
      if (targets.isEmpty) {
        destroyBuildings(state)
      }
      else {
        state.toAttack = Some(targets.minBy(_.pixelDistanceFast(exit)))
        Attack.delegate(state)
      }
    }
    else {
      // Hang out if we can
      if (enemies.exists(_.isBeingViolentTo(state.unit)) || enemies.exists(_.pixelDistanceFast(exit) < state.unit.pixelDistanceFast(exit))) {
        Retreat.delegate(state)
      } else {
        destroyBuildings(state)
        
      }
    }
  }
  
  private def destroyBuildings(state: ActionState) {
    val freebies = state.targets.filter( ! _.canAttackThisSecond(state.unit)).toList.sortBy(_.pixelDistanceFast(state.toTravel.get))
    state.toAttack = freebies.headOption
    Attack.delegate(state)
  }
}
