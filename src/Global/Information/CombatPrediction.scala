package Global.Information

import Startup.With

import scala.collection.JavaConverters._
import Utilities.EnrichIterable._
import Utilities.EnrichUnit._

import scala.collection.mutable

class CombatPrediction {
  
  val combatRange = 32 * 15
  def onFrame() {
    
    //Assign to groups based on proximity to combat
    //Identify line of engagement
    //Simulate combat in tug-of-war tournament
    
    //Warning: Ignores fogged units!
    
    val ourGroups = _groupUnits(With.ourUnits)
    val enemyGroups = _groupUnits(With.enemyUnits)
    
    
      
      
  }
  
  def _mapUnitsToNeighbors(units:Iterable[bwapi.Unit]):Map[bwapi.Unit, Iterable[bwapi.Unit]] = {
    units.map(x => (x, x.getUnitsInRadius(combatRange).asScala)).toMap
  }
  
  def _groupUnits(units:Iterable[bwapi.Unit]):mutable.HashMap[bwapi.Unit, mutable.HashSet[bwapi.Unit]] = {
    val neighborsByFighter = _mapUnitsToNeighbors(With.ourUnits.filter(_.canFight))
    val fightersClosestToEnemyFighters = neighborsByFighter.keys
      .toList
      .sortBy(fighter => neighborsByFighter(fighter)
        .filter(_.isEnemyOf(fighter))
        .filter(_.canAttack)
        .map(_.getDistance(fighter))
        .minOption
        .getOrElse(Int.MaxValue))
  
    val leaderBySoldier = new mutable.HashMap[bwapi.Unit, bwapi.Unit]
    val groupsByLeader = new mutable.HashMap[bwapi.Unit, mutable.HashSet[bwapi.Unit]] {
      override def default(key: bwapi.Unit):mutable.HashSet[bwapi.Unit] = new mutable.HashSet[bwapi.Unit] }
  
    fightersClosestToEnemyFighters.foreach(leader => {
      if (leaderBySoldier.lacks(leader)) {
        leaderBySoldier.put(leader, leader)
        groupsByLeader(leader) ++= neighborsByFighter(leader)
        neighborsByFighter(leader).foreach(neighbor => leaderBySoldier.put(neighbor, leader))
      }})
    
    return groupsByLeader
  }
}
