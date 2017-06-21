package Information.Battles

import Information.Battles.Clustering.BattleClustering
import Information.Battles.Types.{Battle, Team}
import Information.Geography.Types.Zone
import Lifecycle.With
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}

class BattleClassifier {
  
  var global  : Battle                = _
  var byZone  : Map[Zone, Battle]     = Map.empty
  var byUnit  : Map[UnitInfo, Battle] = Map.empty
  var local   : Vector[Battle]        = Vector.empty
  
  def all: Traversable[Battle] = local ++ byZone.values :+ global
  
  private val clustering = new BattleClustering
  
  def classify() {
    clustering.enqueue(With.units.all.filter(isCombatantLocal))
    clustering.run()
    replaceBattleGlobal()
    replaceBattleByZone()
    replaceBattlesLocal()
    all.foreach(BattleUpdater.updateBattle)
  }
  
  private def isCombatantLocal(unit: UnitInfo): Boolean = {
    ! unit.player.isNeutral && isCombatantGlobal(unit) && unit.likelyStillThere
  }
  
  private def isCombatantZone(unit: UnitInfo): Boolean = {
    isCombatantGlobal(unit) && unit.possiblyStillThere
  }
  
  private def isCombatantGlobal(unit: UnitInfo): Boolean = {
    (unit.complete || unit.unitClass.isBuilding) && unit.unitClass.helpsInCombat
  }
  
  private def replaceBattleGlobal() {
    val oldGlobal = global
    global = new Battle(
      new Team(upcastOurs  (With.units.ours  .filter(isCombatantGlobal))),
      new Team(upcastEnemy (With.units.enemy .filter(isCombatantGlobal))))
    if (oldGlobal != null) {
      adoptMetrics(oldGlobal, global)
    }
  }
  
  private def replaceBattleByZone() {
    val combatantsOursByZone  = With.units.ours.filter(isCombatantZone).groupBy(_.tileIncludingCenter.zone)
    val combatantsEnemyByZone = With.units.enemy.filter(isCombatantZone).groupBy(_.tileIncludingCenter.zone)
    val oldByZone = byZone
    byZone = With.geography.zones
      .map(zone => (
        zone,
        new Battle(
          new Team(upcastOurs  (combatantsOursByZone .getOrElse(zone, Vector.empty))),
          new Team(upcastEnemy (combatantsEnemyByZone.getOrElse(zone, Vector.empty)))
        )))
      .toMap
    
    if (oldByZone.nonEmpty) {
      With.geography.zones.foreach(zone => adoptMetrics(oldByZone(zone), byZone(zone)))
    }
  }
  
  private def replaceBattlesLocal() {
    if ( ! With.units.enemy.exists(isCombatantLocal)) {
      local = Vector.empty
      byUnit = Map.empty
      return
    }
    val localNew = buildBattlesLocal
    localNew.foreach(adoptExistingLocalBattleMetrics)
    local = localNew
    byUnit = local
      .flatten(battle =>
        Vector(
          battle.us.units,
          battle.enemy.units)
        .flatten
        .map(unit => (unit, battle)))
      .toMap
  }
  
  private def adoptExistingLocalBattleMetrics(battle: Battle) {
    val existingBattles = (battle.us.units ++ battle.enemy.units).groupBy(byUnit.get).filter(_._1.nonEmpty)
    if (existingBattles.nonEmpty) {
      adoptMetrics(existingBattles.maxBy(_._2.size)._1.get, battle)
    }
  }
  
  private def buildBattlesLocal: Vector[Battle] = {
    clustering.clusters
      .map(cluster =>
        new Battle(
          new Team(cluster.filter(_.isOurs).toVector),
          new Team(cluster.filter(_.isEnemy).toVector)))
      .filter(battle => {
        battle.us.units.nonEmpty &&
        battle.enemy.units.nonEmpty
      })
      .toVector
  }
  
  private def adoptMetrics(oldBattle: Battle, newBattle: Battle) {
    newBattle.estimationGeometric = oldBattle.estimationGeometric
  }
  
  private def upcastOurs  (units: Traversable[FriendlyUnitInfo]) : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
  private def upcastEnemy (units: Traversable[ForeignUnitInfo])  : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
}
