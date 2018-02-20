package Micro.Actions.Combat.Tactics

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
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.enemies.exists(_.raceInitial == Race.Terran)             &&
    unit.agent.canFight                                           &&
    unit.canMove                                                  &&
    unit.is(Protoss.Dragoon)                                      &&
    With.self.hasUpgrade(Protoss.DragoonRange)                    &&
    unit.matchups.threats.forall( ! _.is(Terran.SiegeTankSieged)) &&
    unit.matchups.targets.exists(target =>
      target.visible            &&
      target.aliveAndComplete   &&
      target.is(Terran.Bunker)  &&
      ! target.player.hasUpgrade(Terran.MarineRange))
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    // Goal: Take down the bunker. Don't take any damage from it.
    // If we're getting shot at by the bunker, back off.
    val bunkers = unit.matchups.threats.filter(_.is(Terran.Bunker))
    if (unit.damageInLastSecond > 0) {
      Leave.delegate(unit)
    }
    else if (bunkers.exists(unit.inRangeToAttack)) {
      With.commander.hold(unit)
    }
  }
}
