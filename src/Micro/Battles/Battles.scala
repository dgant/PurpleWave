package Micro.Battles

import ProxyBwapi.UnitInfo.UnitInfo
import Geometry.Clustering
import Performance.Caching.Limiter
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._

import scala.collection.mutable

class Battles {
  
  val all = new mutable.HashSet[Battle]
  
  val byUnit = new mutable.HashMap[UnitInfo, Battle]
  
  def onFrame() = updateLimiter.act()
  private val updateLimiter = new Limiter(2, update)
  private def update() {
    byUnit.clear()
    defineBattles()
    all.foreach(update)
    all.filterNot(isValid).foreach(all.remove)
    all.foreach(battle => {
      battle.enemy.units.foreach(unit => byUnit.put(unit, battle))
      battle.us   .units.foreach(unit => byUnit.put(unit, battle))
    })
  }
  
  private def update(battle:Battle) {
    val groups = List(battle.us, battle.enemy)
    groups.foreach(group => group.units.filterNot(_.alive).foreach(group.units.remove))
    if ( ! isValid(battle)) return
    groups.foreach(group => group.center = BattleMetrics.center(group))
    battle.us.vanguard    = battle.us.units.minBy(_.pixelCenter.distancePixelsSquared(battle.enemy.center)).pixelCenter
    battle.enemy.vanguard = battle.enemy.units.minBy(_.pixelCenter.distancePixelsSquared(battle.us.center)).pixelCenter
    groups.foreach(group => group.strength = BattleMetrics.evaluate(group, battle))
  }
  
  def isValid(battle:Battle):Boolean = {
    battle.us.units.nonEmpty && battle.enemy.units.nonEmpty
  }
  
  private def defineBattles() {
    val battleRange   = 32 * 18
    val ourClusters   = Clustering.groupUnits(getFighters(With.units.ours),  battleRange).values
    val enemyClusters = Clustering.groupUnits(getFighters(With.units.enemy), battleRange).values
    val ourGroups     = ourClusters  .map(group => new BattleGroup(group))
    val enemyGroups   = enemyClusters.map(group => new BattleGroup(group))
    assignBattles(ourGroups, enemyGroups)
  }
  
  private def getFighters(units:Iterable[UnitInfo]):Iterable[UnitInfo] = {
    units.filter(u => u.possiblyStillThere && u.alive && u.impactsCombat)
  }
  
  private def assignBattles(
    ourGroups:Iterable[BattleGroup],
    theirGroups:Iterable[BattleGroup]) {
    
    all.clear()
  
    if (theirGroups.isEmpty) return
    
    (ourGroups ++ theirGroups).foreach(group => group.center = BattleMetrics.center(group))
    
    ourGroups
      .groupBy(ourGroup => theirGroups.minBy(_.center.distancePixelsSquared(ourGroup.center)))
      .map(pair => new Battle(mergeGroups(pair._2), pair._1))
      .foreach(all.add)
  }
  
  private def mergeGroups(groups:Iterable[BattleGroup]):BattleGroup = {
    val output = new BattleGroup(new mutable.HashSet)
    groups.foreach(output.units ++= _.units)
    output
  }
}
