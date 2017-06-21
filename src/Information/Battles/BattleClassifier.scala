package Information.Battles

import Information.Battles.Clustering.BattleClustering
import Information.Battles.Types.{Battle, Team}
import Information.Geography.Types.Zone
import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}

class BattleClassifier {
  
  var global  : Battle                = _
  var byZone  : Map[Zone, Battle]     = Map.empty
  var byUnit  : Map[UnitInfo, Battle] = Map.empty
  var local   : Vector[Battle]        = Vector.empty
  
  def all: Traversable[Battle] = local ++ byZone.values :+ global
  
  private val clustering = new BattleClustering
  
  def run() {
    clustering.enqueue(With.units.all.filter(isCombatantLocal))
    clustering.run()
    replaceBattleGlobal()
    replaceBattlesByZone()
    replaceBattlesLocal()
    BattleUpdater.run()
  }
  
  private def isCombatantLocal(unit: UnitInfo): Boolean = {
    isCombatantGlobal(unit) && unit.likelyStillThere
  }
  
  private def isCombatantZone(unit: UnitInfo): Boolean = {
    isCombatantGlobal(unit) && unit.possiblyStillThere
  }
  
  private def isCombatantGlobal(unit: UnitInfo): Boolean = {
    unit.alive                                    &&
    (unit.complete || unit.unitClass.isBuilding)  &&
    unit.unitClass.helpsInCombat                  &&
    ! unit.player.isNeutral                       &&
    ! unit.is(Terran.SpiderMine)                  &&
    ! unit.is(Protoss.Scarab)                     &&
    ! unit.is(Protoss.Interceptor)
  }
  
  private def replaceBattleGlobal() {
    global = new Battle(
      new Team(asVectorUs     (With.units.ours  .filter(isCombatantGlobal))),
      new Team(asVectorEnemy  (With.units.enemy .filter(isCombatantGlobal))))
  }
  
  private def replaceBattlesByZone() {
    val combatantsOursByZone  = With.units.ours   .filter(isCombatantZone).groupBy(_.tileIncludingCenter.zone)
    val combatantsEnemyByZone = With.units.enemy  .filter(isCombatantZone).groupBy(_.tileIncludingCenter.zone)
    byZone = With.geography.zones
      .map(zone => (
        zone,
        new Battle(
          new Team(asVectorUs     (combatantsOursByZone .getOrElse(zone, Vector.empty))),
          new Team(asVectorEnemy  (combatantsEnemyByZone.getOrElse(zone, Vector.empty)))
        )))
      .toMap
  }
  
  private def replaceBattlesLocal() {
    local = clustering.clusters
      .map(cluster =>
        new Battle(
          new Team(cluster.filter(_.isOurs).toVector),
          new Team(cluster.filter(_.isEnemy).toVector)))
      .filter(_.happening)
      .toVector
    byUnit = local.flatten(battle => battle.teams.flatMap(_.units).map(unit => (unit, battle))).toMap
  }
  
  private def asVectorUs    (units: Traversable[FriendlyUnitInfo]) : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
  private def asVectorEnemy (units: Traversable[ForeignUnitInfo])  : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
}
