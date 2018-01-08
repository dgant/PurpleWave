package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object KiteSafely extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.unitClass.ranged
    && unit.matchups.targets.nonEmpty
    && unit.matchups.threats.nonEmpty
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    
    if (unit.readyForAttackOrder) {
      
      if (unit.matchups.framesOfSafetyDiffused > 1.2 * unit.unitClass.framesToTurnAndShootAndTurnBackAndAccelerate) {
        Potshot.consider(unit)
        Target.consider(unit)
        Attack.consider(unit)
      }
      
      // If we're not going anywhere, might as well shoot.
      //
      if (unit.seeminglyStuck) {
        Potshot.consider(unit)
      }
      
      // If we're getting run down, might as well get some shots off
      //
      if (unit.matchups.threatsInRange.exists(_.topSpeedChasing > unit.topSpeed)) {
        Potshot.consider(unit)
      }
      
      // If shooting is super cheap, might as well shoot
      //
      if (unit.unitClass.stopFrames <= 2) {
        Potshot.consider(unit)
      }
    }
    
    OldAvoid.delegate(unit)
  }
}
