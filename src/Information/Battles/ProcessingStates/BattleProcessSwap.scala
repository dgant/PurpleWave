package Information.Battles.ProcessingStates

import Information.Battles.{BattleClassificationFilters, GlobalSafeToMoveOut}
import Information.Battles.Types.{BattleGlobal, Team}
import Lifecycle.With

class BattleProcessSwap extends BattleProcessState {
  override def step(): Unit = {

    // Replace local
    With.battles.local = With.battles.nextBattlesLocal
    With.battles.byUnit = With.battles.local.flatten(battle => battle.teams.flatMap(_.units).map(unit => (unit, battle))).toMap
    With.battles.divisions = With.battles.nextDivisions

    // Replace global
    With.battles.nextBattleGlobal.foreach(With.battles.global = _)
    With.battles.global = new BattleGlobal(
      new Team(With.units.ours  .view.filter(BattleClassificationFilters.isEligibleGlobal).toVector),
      new Team(With.units.enemy .view.filter(BattleClassificationFilters.isEligibleGlobal).toVector))
    With.blackboard.safeToMoveOut.set(GlobalSafeToMoveOut())

    transitionTo(new BattleProcessMatchupAnalysis)
  }
}
