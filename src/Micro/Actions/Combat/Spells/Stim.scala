package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Stim extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.hitPoints >= 15                                &&
    unit.transport.isEmpty                              &&
    (unit.is(Terran.Marine) || unit.is(Terran.Firebat)) &&
    With.self.hasTech(Terran.Stim)                      &&
    ! unit.stimmed                                      && // TODO: stimFrames < With.latency.framesRemaining
    (unit.matchups.targetsInRange.nonEmpty || unit.matchups.threatsInRange.nonEmpty || unit.agent.shouldEngage)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    With.commander.useTech(unit, Terran.Stim)
  }
}
