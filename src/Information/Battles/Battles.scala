package Information.Battles

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Pixels.Tile
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Battles {
  
  private val delayLength = 1
  
  private var combatantsOurs  : Set[FriendlyUnitInfo] = Set.empty
  private var combatantsEnemy : Set[ForeignUnitInfo]  = Set.empty
          var global          : Battle                = null
          var byZone          : Map[Zone, Battle]     = Map.empty
          var byUnit          : Map[UnitInfo, Battle] = Map.empty
          var local           : Vector[Battle]        = Vector.empty
  
  def classify() {
    combatantsOurs  = With.units.ours .filter(unit => unit.unitClass.helpsInCombat)
    combatantsEnemy = With.units.enemy.filter(unit => unit.unitClass.helpsInCombat && unit.possiblyStillThere)
    buildBattleGlobal()
    buildBattlesByZone()
    buildBattlesLocal()
  }
  
  def assess() {
    BattleUpdater.assess(local ++ byZone.values :+ global)
  }
  
  private def buildBattleGlobal() {
    global = new Battle(
      new BattleGroup(upcastOurs(combatantsOurs)),
      new BattleGroup(upcastEnemy(combatantsEnemy)))
  }
  
  private def buildBattlesByZone() {
    val combatantsOursByZone  = combatantsOurs .groupBy(_.tileIncludingCenter.zone)
    val combatantsEnemyByZone = combatantsEnemy.groupBy(_.tileIncludingCenter.zone)
    byZone = With.geography.zones
      .map(zone => (
        zone,
        new Battle(
          new BattleGroup(upcastOurs(combatantsOursByZone .getOrElse(zone, Set.empty))),
          new BattleGroup(upcastEnemy(combatantsEnemyByZone.getOrElse(zone, Set.empty)))
        )))
      .toMap
  }
  
  private def buildBattlesLocal() {
    if (combatantsEnemy.isEmpty) {
      byUnit = Map.empty
      return
    }
    
    val framesToLookAhead = Math.max(12, 2 * With.performance.cacheLength(delayLength))
    val unassigned = mutable.HashSet.empty ++ combatantsOurs ++ combatantsEnemy
    val clusters = new ArrayBuffer[ArrayBuffer[UnitInfo]]
    
    val exploredTiles = new mutable.HashSet[Tile]
    val horizonTiles = new mutable.HashSet[Tile]
    //val searchMarginTiles = 2
    val searchRadiusTiles = With.configuration.combatEvaluationDistanceTiles //searchMarginTiles + Math.min(With.configuration.combatEvaluationDistanceTiles, unassigned.map(_.pixelReachTotal(framesToLookAhead)).max/32 + 1)
    
    while (unassigned.nonEmpty) {
      
      val nextCluster = new ArrayBuffer[UnitInfo]
      horizonTiles.add(unassigned.head.tileIncludingCenter)
      
      while (horizonTiles.nonEmpty) {
        
        val nextTile = horizonTiles.head
        horizonTiles.remove(nextTile)
        exploredTiles.add(nextTile)
        
        val nextUnits = With.grids.units.get(nextTile)
        nextUnits.foreach(unassigned.remove)
        nextCluster ++= nextUnits
        
        if (nextUnits.nonEmpty) {
          horizonTiles ++=
            Circle.points(searchRadiusTiles)
            .map(nextTile.add)
            .filterNot(exploredTiles.contains)
        }
      }
      
      clusters.append(nextCluster)
    }
    local =
      clusters
        .map(cluster =>
          new Battle(
            new BattleGroup(cluster.filter(_.isOurs).toSet),
            new BattleGroup(cluster.filter(_.isEnemy).toSet)))
        .filter(battle =>
          battle.us.units.nonEmpty &&
          battle.enemy.units.nonEmpty)
        .toVector
    byUnit = local
      .flatten(battle =>
        Vector(
          battle.us.units,
          battle.enemy.units)
        .flatten
        .map(unit => (unit, battle)))
      .toMap
  }

  def upcastOurs  (units:Set[FriendlyUnitInfo]) : Set[UnitInfo] = units.map(_.asInstanceOf[UnitInfo])
  def upcastEnemy (units:Set[ForeignUnitInfo])  : Set[UnitInfo] = units.map(_.asInstanceOf[UnitInfo])
}
