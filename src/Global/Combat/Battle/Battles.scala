package Global.Combat.Battle

import Geometry.Clustering
import Startup.With
import Types.UnitInfo.UnitInfo
import Utilities.Limiter

import scala.collection.mutable

class Battles {
  
  val battleRange = 32 * 16
  var battles = new mutable.HashSet[Battle]
  
  val limitBattleDefinition = new Limiter(6, _defineBattles)
  def onFrame() {
    limitBattleDefinition.act()
    battles.foreach(BattleMetrics.update)
    battles.filterNot(BattleMetrics.isValid).foreach(battles.remove)
  }
  
  def _defineBattles() {
    val ourGroupMaps = Clustering.groupUnits(_getFighters(With.units.ours), battleRange)
    val enemyGroupMaps = Clustering.groupUnits(_getFighters(With.units.enemy), battleRange)
    val ourGroups = ourGroupMaps.map(group => new BattleGroup(group._1.position, group._2))
    val enemyGroups = enemyGroupMaps.map(group => new BattleGroup(group._1.position, group._2))
    _assignBattles(ourGroups, enemyGroups)
  }
  
  def _getFighters(units:Iterable[UnitInfo]):Iterable[UnitInfo] = {
    units.filterNot(_.utype.isWorker).filter(_.canFight)
  }
  
  def _assignBattles(
    ourGroups:Iterable[BattleGroup],
    theirGroups:Iterable[BattleGroup]) {
    
    battles.clear()
    
    if (ourGroups.nonEmpty && theirGroups.nonEmpty) {
      ourGroups.filter(_.units.exists(_.alive))
        .map(ourGroup => {
          val enemyGroup = theirGroups.filter(_.units.exists(_.alive)).minBy(_.vanguard.getDistance(ourGroup.vanguard))
          val simulation = new Battle(ourGroup, enemyGroup)
          simulation
        })
        .foreach(battles.add(_))
    }
  }
}
