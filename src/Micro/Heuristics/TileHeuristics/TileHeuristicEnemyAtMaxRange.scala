package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.TilePosition

object TileHeuristicEnemyAtMaxRange extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
    val us = intent.unit
    val dangerousEnemies = intent.targets.filter(_.impactsCombat)
    val kiteableEnemies = dangerousEnemies.filter(isKiteable(intent.unit, _))
    
    if (dangerousEnemies.isEmpty) return 1.0
    
    val nearestDangerousEnemy = dangerousEnemies.minBy(effectiveDistance(us, _))
    
    if ( ! isKiteable(us, nearestDangerousEnemy)) return 1.0
    
    Math.max(1.0, effectiveDistance(us, nearestDangerousEnemy))
  }
  
  
  def isKiteable(us:UnitInfo, enemy: UnitInfo):Boolean = {
    enemy.unitClass.maxAirGroundRange < us.unitClass.maxAirGroundRange
  }
  
  def effectiveDistance(us:UnitInfo, enemy:UnitInfo):Double = {
    enemy.pixelDistance(us) - enemy.unitClass.maxAirGroundRange
  }
  
}
