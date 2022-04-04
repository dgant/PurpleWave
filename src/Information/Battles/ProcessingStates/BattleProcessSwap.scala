package Information.Battles.ProcessingStates

import Information.Battles.BattleFilters
import Information.Battles.Types.Battle
import Lifecycle.With

class BattleProcessSwap extends BattleProcessState {
  override def step(): Unit = {

    // Replace local
    With.battles.local = With.battles.nextBattlesLocal
    With.battles.byUnit = With.battles.local.flatten(battle => battle.teams.flatMap(_.units).map(unit => (unit, battle))).toMap
    With.battles.divisions = With.battles.nextDivisions

    // Replace global
    With.battles.globalHome = With.battles.nextBattleGlobalHome
    With.battles.globalAway = With.battles.nextBattleGlobalAway
    With.battles.nextBattleGlobalHome = new Battle(
      With.units.ours  .view.filter(BattleFilters.global).filter(BattleFilters.home).toVector,
      With.units.enemy .view.filter(BattleFilters.global).filter(BattleFilters.home).toVector,
      isGlobal = true)
    With.battles.nextBattleGlobalAway = new Battle(
      With.units.ours  .view.filter(BattleFilters.global).filter(BattleFilters.away).toVector,
      With.units.enemy .view.filter(BattleFilters.global).filter(BattleFilters.away).toVector,
      isGlobal = true)

    transitionTo(new BattleProcessMatchupAnalysis)
  }
}
