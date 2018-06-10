package Micro.Actions.Combat.Tactics

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Disengage
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Detect extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.unitClass.isDetector
    && unit.teammates.exists(_.canAttack)
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    
    val allSpookies = unit.teammates.flatMap(teammate =>
      teammate.matchups.enemies.filter(enemy =>
        teammate.unitClass.attacks(enemy.unitClass)
        && enemy.cloaked
        && teammate.framesToGetInRange(enemy) < GameTime(0, 10)()))
    
    val superSpookies = allSpookies.filter(e =>
      e.effectivelyCloaked
      && e.matchups.targets.nonEmpty)
    
    if (superSpookies.isEmpty && unit.matchups.framesOfSafety <= 0) {
      Disengage.delegate(unit)
    }
    
    val finalSpookies = if (superSpookies.isEmpty) allSpookies else superSpookies
    
    if (finalSpookies.isEmpty) return
    
    val spookiest = finalSpookies.minBy(x => ( ! x.canAttack, ! x.effectivelyCloaked, x.pixelDistanceEdge(unit)))
    val vantage   = spookiest.pixelCenter.project(unit.agent.destination, 32.0 * 5.0)
    unit.agent.toTravel = Some(vantage)
    Move.delegate(unit)
  }
}
