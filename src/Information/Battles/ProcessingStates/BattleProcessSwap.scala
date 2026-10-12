package Information.Battles.ProcessingStates

import Information.Battles.BattleFilters
import Information.Battles.Types.Battle
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

class BattleProcessSwap extends BattleProcessState {
  override def step(): Unit = {

    // Replace local
    With.battles.local = With.battles.nextBattlesLocal
    With.battles.byUnit = With.battles.local.flatten(battle => battle.teams.flatMap(_.units).map(unit => (unit, battle))).toMap
    With.battles.divisions = With.battles.nextDivisions

    if (With.configuration.debugging && With.configuration.logCombatForecasts) Debugging.CombatForecastDiagnostics.cycle()

    // Replace global
    With.battles.globalDefend   = With.battles.nextBattleGlobalDefend
    With.battles.globalAttack   = With.battles.nextBattleGlobalAttack
    With.battles.globalSlug     = With.battles.nextBattleGlobalSlug
    With.battles.globalSkirmish = With.battles.nextBattleGlobalSkirmish

    val unitsGlobalOurs   = partitionGlobal(With.units.ours)
    val unitsGlobalEnemy  = partitionGlobal(With.units.enemy)
    With.battles.nextBattleGlobalDefend = new Battle(
      unitsGlobalOurs._1,
      unitsGlobalEnemy._1,
      isGlobal = true)
    With.battles.nextBattleGlobalAttack = new Battle(
      unitsGlobalOurs._2,
      unitsGlobalEnemy._2,
      isGlobal = true)
    With.battles.nextBattleGlobalSlug = new Battle(
      unitsGlobalOurs._3,
      unitsGlobalEnemy._3,
      isGlobal = true)
    With.battles.nextBattleGlobalSkirmish = new Battle(
      unitsGlobalOurs._4,
      unitsGlobalEnemy._4,
      isGlobal = true)


    transitionTo(new BattleProcessMatchupAnalysis)
  }

  private def partitionGlobal(units: Iterable[UnitInfo]): (Vector[UnitInfo], Vector[UnitInfo], Vector[UnitInfo], Vector[UnitInfo]) = {
    val defending = new ArrayBuffer[UnitInfo]
    val attacking = new ArrayBuffer[UnitInfo]
    val slugging = new ArrayBuffer[UnitInfo]
    val skirmish = new ArrayBuffer[UnitInfo]
    units.foreach(unit => {
      if (BattleFilters.global(unit)) {
        if (BattleFilters.defending(unit)) defending += unit
        if (BattleFilters.attacking(unit)) attacking += unit
        if (BattleFilters.slugging(unit)) slugging += unit
        if (BattleFilters.skirmish(unit)) skirmish += unit
      }
    })
    (defending.toVector, attacking.toVector, slugging.toVector, skirmish.toVector)
  }
}
