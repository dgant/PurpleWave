package Micro.Actions.Combat.Tactics

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Leave
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import bwapi.Race

object Bust extends Action {
  
  // TODO:
  // jaj22: quick bunker test, range goon has at least a 33 pixel variation for where it stops on a straight line attack command.
  // Conclusion: We need to identify where Dragoons are supposed to stand to shoot at a Bunker, then tell them to Hold Position
  
  // Killing bunkers with Dragoons is an important technique that we can't yet perform on first princples.
  // Range-upgraded Dragoons just barely outrange a Bunker containing non-range-upgraded Marines.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.totalHealth >= unit.unitClass.maxHitPoints + 12
    && With.enemies.exists(_.raceInitial == Race.Terran)
    && unit.agent.canFight
    && unit.canMove
    && unit.is(Protoss.Dragoon)
    && With.self.hasUpgrade(Protoss.DragoonRange)
    && unit.matchups.threatsInRange.forall( ! _.is(Terran.SiegeTankSieged))
    && unit.matchups.targets.exists(target =>
        target.visible
        && target.aliveAndComplete
        && target.is(Terran.Bunker)
        && ! target.player.hasUpgrade(Terran.MarineRange))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    // Goal: Take down the bunker. Don't take any damage from it.
    // If we're getting shot at by the bunker, back off.
    val bunkers = unit.matchups.threats.filter(_.is(Terran.Bunker))
    if (With.framesSince(unit.lastFrameTakingDamage) < GameTime(0, 1)()) {
      Leave.delegate(unit)
    }
    else if (unit.matchups.targetsInRange.nonEmpty) {
      With.commander.hold(unit)
    }
  }
}
