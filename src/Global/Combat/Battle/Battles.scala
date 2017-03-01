package Global.Combat.Battle

import Geometry.Clustering
import Startup.With
import Types.UnitInfo.UnitInfo
import Utilities.Limiter
import Utilities.Enrichment.EnrichPosition._

import scala.collection.mutable

class Battles {
  
  val all = new mutable.HashSet[Battle]
  
  val limitBattleDefinition = new Limiter(6, _defineBattles)
  def onFrame() {
    limitBattleDefinition.act()
    all.foreach(update)
    all.filterNot(isValid).foreach(all.remove)
  }
  
  def update(battle:Battle) {
    List(battle.us, battle.enemy).foreach(group => {
      group.units.filterNot(_.alive).foreach(group.units.remove)
      if (isValid(battle)) {
        group.strength        = BattleMetrics.evaluate(group, battle)
        group.center          = BattleMetrics.center(group)
        group.expectedSpread  = BattleMetrics.expectedSpread(group)
        group.spread          = BattleMetrics.actualSpread(group)
      }})
    if (isValid(battle)) {
      battle.us.vanguard = battle.us.units.minBy(_.position.distanceSquared(battle.enemy.vanguard)).position
      battle.enemy.vanguard = battle.enemy.units.minBy(_.position.distanceSquared(battle.us.vanguard)).position
    }
  }
  
  def isValid(battle:Battle):Boolean = {
    battle.us.units.nonEmpty && battle.enemy.units.nonEmpty
  }
  
  def _defineBattles() {
    val battleRange = 32 * 16
    val ourGroupMaps = Clustering.groupUnits(_getFighters(With.units.ours), battleRange)
    val enemyGroupMaps = Clustering.groupUnits(_getFighters(With.units.enemy), battleRange)
    val ourGroups = ourGroupMaps.map(group => new BattleGroup(group._1.position, group._2))
    val enemyGroups = enemyGroupMaps.map(group => new BattleGroup(group._1.position, group._2))
    _assignBattles(ourGroups, enemyGroups)
  }
  
  def _getFighters(units:Iterable[UnitInfo]):Iterable[UnitInfo] = {
    units.filter(u => u.possiblyStillThere && u.canFight && ! u.utype.isWorker)
  }
  
  def _assignBattles(
    ourGroups:Iterable[BattleGroup],
    theirGroups:Iterable[BattleGroup]) {
    
    all.clear()
    
    if (theirGroups.nonEmpty) {
      ourGroups.filter(_.units.exists(_.alive))
        .map(ourGroup => {
          val enemyGroup = theirGroups.filter(_.units.exists(_.alive)).minBy(_.vanguard.getDistance(ourGroup.vanguard))
          new Battle(ourGroup, enemyGroup)
        })
        .foreach(all.add)
    }
  }
}
