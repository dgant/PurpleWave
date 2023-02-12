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
    With.battles.globalDefend   = With.battles.nextBattleGlobalDefend
    With.battles.globalAttack   = With.battles.nextBattleGlobalAttack
    With.battles.globalSlug     = With.battles.nextBattleGlobalSlug
    With.battles.globalSkirmish = With.battles.nextBattleGlobalSkirmish

    val unitsGlobalOurs   = With.units.ours  .view.filter(BattleFilters.global)
    val unitsGlobalEnemy  = With.units.enemy.view.filter(BattleFilters.global)
    With.battles.nextBattleGlobalDefend = new Battle(
      unitsGlobalOurs.filter(BattleFilters.defending).toVector,
      unitsGlobalEnemy.filter(BattleFilters.defending).toVector,
      isGlobal = true)
    With.battles.nextBattleGlobalAttack = new Battle(
      unitsGlobalOurs.filter(BattleFilters.attacking).toVector,
      unitsGlobalEnemy.filter(BattleFilters.attacking).toVector,
      isGlobal = true)
    With.battles.nextBattleGlobalSlug = new Battle(
      unitsGlobalOurs.filter(BattleFilters.slugging).toVector,
      unitsGlobalEnemy.filter(BattleFilters.slugging).toVector,
      isGlobal = true)
    With.battles.nextBattleGlobalSkirmish = new Battle(
      unitsGlobalOurs.filter(BattleFilters.skirmish).toVector,
      unitsGlobalEnemy.filter(BattleFilters.skirmish).toVector,
      isGlobal = true)


    transitionTo(new BattleProcessMatchupAnalysis)
  }
}
