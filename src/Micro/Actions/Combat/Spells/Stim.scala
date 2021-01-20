package Micro.Actions.Combat.Spells

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Stim extends Action {

  val stimHPThreshold = 15
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.hitPoints >= stimHPThreshold
    && unit.transport.isEmpty
    && unit.canStim
    && ! unit.stimmed // TODO: stimFrames < With.latency.framesRemaining
    && (unit.matchups.targetsInRange.nonEmpty || unit.matchups.threatsInRange.nonEmpty)
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Commander.useTech(unit, Terran.Stim)
  }
}
