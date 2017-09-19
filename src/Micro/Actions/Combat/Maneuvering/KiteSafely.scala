package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.{Potshot, Target}
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
      // If we're not going anywhere, might as well shoot
      if (unit.velocity.lengthSquared == 0) {
        Potshot.consider(unit)
      }
    }
    
    Avoid.delegate(unit)
  }
}
