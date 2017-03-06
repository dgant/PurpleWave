package Global.Combat.Battle

import Geometry.Clustering
import Startup.With
import Types.UnitInfo.UnitInfo
import Utilities.Caching.Limiter
import Utilities.Enrichment.EnrichPosition._

import scala.collection.mutable

class Battles {
  
  val all = new mutable.HashSet[Battle]
  
  val _limitUpdates = new Limiter(12, _update)
  def onFrame() = _limitUpdates.act()
  
  def update(battle:Battle) {
    val groups = List(battle.us, battle.enemy)
    groups.foreach(group => group.units.filterNot(_.alive).foreach(group.units.remove))
    if ( ! isValid(battle)) {
      return
    }
    groups.foreach(group => {
      group.center          = BattleMetrics.center(group)
    })
    battle.us.vanguard    = battle.us.units.minBy(_.position.distanceSquared(battle.enemy.center)).position
    battle.enemy.vanguard = battle.enemy.units.minBy(_.position.distanceSquared(battle.us.center)).position
    groups.foreach(group => group.strength = BattleMetrics.evaluate(group, battle))
  }
  
  def isValid(battle:Battle):Boolean = {
    battle.us.units.nonEmpty && battle.enemy.units.nonEmpty
  }
  
  def _update() {
    _defineBattles()
    all.foreach(update)
    all.filterNot(isValid).foreach(all.remove)
  }
  
  def _defineBattles() {
    val battleRange = 32 * 16
    val ourClusters = Clustering.groupUnits(_getFighters(With.units.ours), battleRange).values
    val enemyClusters = Clustering.groupUnits(_getFighters(With.units.enemy), battleRange).values
    val ourGroups = ourClusters.map(group => new BattleGroup(group))
    val enemyGroups = enemyClusters.map(group => new BattleGroup(group))
    _assignBattles(ourGroups, enemyGroups)
  }
  
  def _getFighters(units:Iterable[UnitInfo]):Iterable[UnitInfo] = {
    units.filter(u => u.possiblyStillThere && u.canFight)
  }
  
  def _assignBattles(
    ourGroups:Iterable[BattleGroup],
    theirGroups:Iterable[BattleGroup]) {
    
    all.clear()
  
    if (theirGroups.isEmpty) return
    
    (ourGroups ++ theirGroups).foreach(group => group.center = BattleMetrics.center(group))
    
    ourGroups
      .groupBy(ourGroup => theirGroups.minBy(_.center.distanceSquared(ourGroup.center)))
      .map(pair => new Battle(_mergeGroups(pair._2), pair._1))
      .foreach(all.add)
  }
  
  def _mergeGroups(groups:Iterable[BattleGroup]):BattleGroup = {
    val output = new BattleGroup(new mutable.HashSet)
    groups.foreach(output.units ++= _.units)
    output
  }
}
