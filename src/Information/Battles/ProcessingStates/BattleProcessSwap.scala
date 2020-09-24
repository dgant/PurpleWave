package Information.Battles.ProcessingStates

import Information.Battles.BattleClassificationFilters
import Information.Battles.Types.{BattleGlobal, BattleLocal, Team}
import Lifecycle.With

class BattleProcessSwap extends BattleProcessState {
  override def step(): Unit = {
    // Replace local
    With.battles.local = With.battles.nextBattlesLocal
    With.battles.byUnit = With.battles.local.flatten(battle => battle.teams.flatMap(_.units).map(unit => (unit, battle))).toMap
    With.battles.nextBattlesLocal = With.battles.clustering.clusters
      .map(cluster =>
        new BattleLocal(
          new Team(cluster.filter(_.isOurs)),
          new Team(cluster.filter(_.isEnemy))))
      .filter(_.teams.forall(_.units.exists(u =>
        u.canAttack
        || u.unitClass.isSpellcaster
        || u.unitClass.isDetector
        || u.unitClass.isTransport)))

    // Replace global
    With.battles.nextBattleGlobal.foreach(With.battles.global = _)
    With.battles.global = new BattleGlobal(
      new Team(With.units.ours  .view.filter(BattleClassificationFilters.isEligibleGlobal).toVector),
      new Team(With.units.enemy .view.filter(BattleClassificationFilters.isEligibleGlobal).toVector))

    // TODO: Transition
  }
}
