package Information.Battles

import Information.Battles.BattleTypes.{Battle, BattleGroup}
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Pixels.Tile
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClassifier {
  
  private var combatantsOurs  : Vector[FriendlyUnitInfo] = Vector.empty
  private var combatantsEnemy : Vector[ForeignUnitInfo]  = Vector.empty
  
  var global  : Battle                = null
  var byZone  : Map[Zone, Battle]     = Map.empty
  var byUnit  : Map[UnitInfo, Battle] = Map.empty
  var local   : Vector[Battle]        = Vector.empty
  
  def all:Traversable[Battle] = local ++ byZone.values :+ global
  
  def classify() {
    combatantsOurs  = With.units.ours .toVector.filter(isCombatant)
    combatantsEnemy = With.units.enemy.toVector.filter(isCombatant)
    replaceBattleGlobal()
    replaceBattleByZone()
    replaceBattlesLocal()
    all.foreach(BattleUpdater.updateBattle)
  }
  
  def isCombatant(unit:UnitInfo):Boolean = {
    (unit.complete || unit.unitClass.isBuilding) &&
      unit.unitClass.helpsInCombat &&
      unit.possiblyStillThere
  }
  
  private def replaceBattleGlobal() {
    val oldGlobal = global
    global = new Battle(
      new BattleGroup(upcastOurs(combatantsOurs)),
      new BattleGroup(upcastEnemy(combatantsEnemy)))
    if (oldGlobal != null) {
      adoptMetrics(oldGlobal, global)
    }
  }
  
  private def replaceBattleByZone() {
    val combatantsOursByZone  = combatantsOurs .groupBy(_.tileIncludingCenter.zone)
    val combatantsEnemyByZone = combatantsEnemy.groupBy(_.tileIncludingCenter.zone)
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
    if (combatantsEnemy.isEmpty) {
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
      .filter(battle =>
        battle.us.units.nonEmpty &&
          battle.enemy.units.nonEmpty)
      .toVector
  }
  
  private def clusterUnits():ArrayBuffer[ArrayBuffer[UnitInfo]] = {
    
    val unassignedUnits = mutable.HashSet.empty ++ (combatantsOurs ++ combatantsEnemy)
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
    newBattle.us.strength     = oldBattle.us.strength
    newBattle.enemy.strength  = oldBattle.enemy.strength
    newBattle.estimations     = oldBattle.estimations
  }

  def upcastOurs  (units:Vector[FriendlyUnitInfo]) : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo])
  def upcastEnemy (units:Vector[ForeignUnitInfo])  : Vector[UnitInfo] = units.map(_.asInstanceOf[UnitInfo])
}
