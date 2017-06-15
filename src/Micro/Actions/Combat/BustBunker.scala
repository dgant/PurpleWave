package Micro.Actions.Combat

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.{Attack, Reposition}
import Micro.Task.ExecutionState
import ProxyBwapi.Races.{Protoss, Terran}

object BustBunker extends Action {
  
  // Killing bunkers with Dragoons is an important technique that we can't yet perform on first princples.
  // Range-upgraded Dragoons just barely outrange a Bunker containing non-range-upgraded Marines.
  // The window for a Dragoon to shoot at a Bunker for free is smaller than the resolution of our damage grid.
  // Thus, we explicitly encode this behavior.
  
  override protected def allowed(state: ExecutionState): Boolean = {
    state.canAttack                                         &&
    state.unit.canMoveThisFrame                             &&
    state.unit.is(Protoss.Dragoon)                          &&
    With.self.hasUpgrade(Protoss.DragoonRange)              &&
    ! With.enemies.exists(_.hasUpgrade(Terran.MarineRange)) &&
    state.threats.forall( ! _.is(Terran.SiegeTankSieged))   &&
    state.targets.exists(target => target.aliveAndComplete && target.is(Terran.Bunker))
    
  }
  
  override protected def perform(state: ExecutionState) {
    
    // Goal: Take down the bunker. Don't take any damage.
    
    // Make darn sure we're not taking bunker damage!
    if (
      state.unit.damageInLastSecond > 0 &&
      state.threats.exists(threat =>
        threat.is(Terran.Bunker) &&
        threat.pixelDistanceSlow(state.unit) < With.configuration.bunkerSafetyMargin)) {
      state.movementProfile.preferThreatDistance = 5.0
      state.movementProfile.avoidDamage = 5.0
      Reposition.delegate(state)
    }
    else {
      state.toAttack = Some(state.targets.minBy(_.pixelDistanceSquared(state.unit)))
      Attack.delegate(state)
    }
  }
}
