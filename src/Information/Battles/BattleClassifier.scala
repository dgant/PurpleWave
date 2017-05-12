package Information.Battles

import Information.Battles.Types.{Battle, BattleGroup}
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Pixels.Tile
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClassifier {
  
  
  var global  : Battle                = null
  var byZone  : Map[Zone, Battle]     = Map.empty
  var byUnit  : Map[UnitInfo, Battle] = Map.empty
  var local   : Vector[Battle]        = Vector.empty
  
  def all:Traversable[Battle] = local ++ byZone.values :+ global
  
  def classify() {
    replaceBattleGlobal()
    replaceBattleByZone()
    replaceBattlesLocal()
    all.foreach(BattleUpdater.updateBattle)
  }
  
  //TODO: Separate "likely sitll theres" based on local/zone/global
  private def isCombatantLocal(unit:UnitInfo):Boolean = {
    isCombatantGlobal(unit) && unit.likelyStillThere
  }
  
  private def isCombatantZone(unit:UnitInfo):Boolean = {
    isCombatantGlobal(unit) && unit.possiblyStillThere
  }
  
  private def isCombatantGlobal(unit:UnitInfo):Boolean = {
    (unit.complete || unit.unitClass.isBuilding) && unit.unitClass.helpsInCombat
  }
  
  private def replaceBattleGlobal() {
    val oldGlobal = global
    global = new Battle(
      new BattleGroup(upcastOurs(With.units.ours.filter(isCombatantGlobal))),
      new BattleGroup(upcastEnemy(With.units.enemy.filter(isCombatantGlobal))))
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
          new BattleGroup(upcastOurs  (combatantsOursByZone .getOrElse(zone, Vector.empty))),
          new BattleGroup(upcastEnemy (combatantsEnemyByZone.getOrElse(zone, Vector.empty)))
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
  
  private def adoptExistingLocalBattleMetrics(battle:Battle) {
    val existingBattles = (battle.us.units ++ battle.enemy.units).groupBy(byUnit.get).filter(_._1.nonEmpty)
    if (existingBattles.nonEmpty) {
      adoptMetrics(existingBattles.maxBy(_._2.size)._1.get, battle)
    }
  }
  
  private def buildBattlesLocal:Vector[Battle] = {
    val clusters = clusterUnits()
    clusters
      .map(cluster =>
        new Battle(
          new BattleGroup(cluster.filter(_.isOurs).toVector),
          new BattleGroup(cluster.filter(_.isEnemy).toVector)))
      .filter(battle => {
        battle.us.units.nonEmpty &&
        battle.enemy.units.nonEmpty
      })
      .toVector
  }
  
  private def clusterUnits():ArrayBuffer[ArrayBuffer[UnitInfo]] = {
    
    val unassignedUnits = mutable.HashSet.empty ++ (With.units.ours ++ With.units.enemy).filter(isCombatantLocal)
    val clusters        = new ArrayBuffer[ArrayBuffer[UnitInfo]]
    val exploredTiles   = new mutable.HashSet[Tile]
    val horizonTiles    = new mutable.HashSet[Tile]
  
    while (unassignedUnits.nonEmpty) {
    
      val firstUnit   = unassignedUnits.head
      
      val nextCluster = new ArrayBuffer[UnitInfo]
      unassignedUnits   -= firstUnit
      nextCluster       += firstUnit
      
      horizonTiles.add(firstUnit.tileIncludingCenter)
    
      while (horizonTiles.nonEmpty) {
      
        val nextTile = horizonTiles.head
        horizonTiles  -=  nextTile
        exploredTiles +=  nextTile
      
        val nextUnits = With.grids.units.get(nextTile).filter(_ != firstUnit)
        unassignedUnits   --= nextUnits
        nextCluster       ++= nextUnits
        
        //Note that this includes non-combatants!
        Circle.points(With.configuration.battleMarginTiles)
          .foreach(point => {
            val tile = nextTile.add(point)
            if (tile.valid
              && ! exploredTiles.contains(tile)
              && With.grids.units.get(tile).nonEmpty)
              horizonTiles += tile
          })
      }
    
      clusters.append(nextCluster)
    }
    
    clusters
  }
  
  def adoptMetrics(oldBattle:Battle, newBattle:Battle) {
    newBattle.estimation = oldBattle.estimation
  }

  def upcastOurs  (units:Traversable[FriendlyUnitInfo]) : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
  def upcastEnemy (units:Traversable[ForeignUnitInfo])  : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo]).toVector
}
