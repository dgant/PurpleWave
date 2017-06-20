package Micro.Actions.Combat

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Execution.ExecutionState
import ProxyBwapi.Races.{Protoss, Terran}
import bwapi.Race

object BustBunker extends Action {
  
  // Killing bunkers with Dragoons is an important technique that we can't yet perform on first princples.
  // Range-upgraded Dragoons just barely outrange a Bunker containing non-range-upgraded Marines.
  // The window for a Dragoon to shoot at a Bunker for free is smaller than the resolution of our damage grid.
  // Thus, we explicitly encode this behavior.
  
  override protected def allowed(state: ExecutionState): Boolean = {
    With.enemies.exists(_.race == Race.Terran)              &&
    state.canFight                                          &&
    state.unit.canMoveThisFrame                             &&
    state.unit.is(Protoss.Dragoon)                          &&
    state.threats.forall( ! _.is(Terran.SiegeTankSieged))   &&
    state.targets.exists(target =>
      target.aliveAndComplete   &&
      target.is(Terran.Bunker)  &&
      ! target.player.hasUpgrade(Terran.MarineRange))
  }
  
  override protected def perform(state: ExecutionState) {
    
    // Goal: Take down the bunker. Don't take any damage from it.
    
    if (
      // If we don't have Dragoon range yet, back off.
      With.units.ours.exists(unit => unit.researching && unit.upgradingType == Protoss.DragoonRange) ||
        
      // If we're getting shot at by the bunker, back off.
      (
        state.unit.damageInLastSecond > 0 &&
        state.threats.exists(threat =>
          threat.is(Terran.Bunker) &&
          threat.pixelDistanceSlow(state.unit) < With.configuration.bunkerSafetyMargin)
      )) {
      HoverOutsideRange.delegate(state)
    }
    else if (With.self.hasUpgrade(Protoss.DragoonRange)) {
      state.toAttack = Some(state.targets.minBy(_.pixelDistanceSquared(state.unit)))
      Attack.delegate(state)
    }
  }
}
