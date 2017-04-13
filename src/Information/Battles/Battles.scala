package Information.Battles

import Information.Geography.Types.Zone
import Lifecycle.With
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}
import Utilities.EnrichPixel._

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

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
    
    val exploredTiles = new mutable.BitSet
    val horizonTiles = new mutable.BitSet
    //val searchMarginTiles = 2
    val searchRadius = With.configuration.combatEvaluationDistanceTiles //searchMarginTiles + Math.min(With.configuration.combatEvaluationDistanceTiles, unassigned.map(_.pixelReachTotal(framesToLookAhead)).max/32 + 1)
    while (unassigned.nonEmpty) {
      val nextCluster = new ArrayBuffer[UnitInfo]
      horizonTiles.add(unassigned.head.tileIncludingCenter.i)
      while (horizonTiles.nonEmpty) {
        val nextTile = horizonTiles.head
        exploredTiles.add(nextTile)
        horizonTiles.remove(nextTile)
        unassigned.remove(nextTile)
        nextCluster.add(nextTile)
        horizonTiles ++= nextTile
          .inTileRadius(With.configuration.combatEvaluationDistanceTiles)
          .filter(unassigned.contains)
          .filter(otherUnit =>
            nextTile.pixelsFromEdgeFast(otherUnit) <=
              32.0 * searchMarginTiles +
              Math.max(
                nextTile.pixelReachTotal(framesToLookAhead),
                otherUnit.pixelReachTotal(framesToLookAhead)))
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
