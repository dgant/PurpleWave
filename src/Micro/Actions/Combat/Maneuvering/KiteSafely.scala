package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object KiteSafely extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.matchups.targets.nonEmpty
    && unit.matchups.threats.nonEmpty
    && unit.ranged
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    
    if (unit.readyForAttackOrder) {
      
      if (unit.matchups.framesOfSafetyDiffused >= unit.framesToTurnAndShootAndTurnBackAndAccelerate + With.latency.latencyFrames) {
        Potshot.consider(unit)
        Target.consider(unit)
        Attack.consider(unit)
      }
      
      // If we're not going anywhere, might as well shoot.
      //
      // There are probably other good ways to detect this.
      if (unit.velocity.lengthSquared == 0 || unit.seeminglyStuck) {
        Potshot.consider(unit)
      }
      
      // If we're getting run down, might as well get some shots off
      //
      if (unit.matchups.threatsInRange.exists(_.topSpeedChasing > unit.topSpeed)) {
        Potshot.consider(unit)
      }
      
      // If shooting is cheap, might as well shoot
      //
      if (unit.unitClass.minStop < 4) {
        Potshot.consider(unit)
      }
    }
    
    Avoid.delegate(unit)
  }
}
