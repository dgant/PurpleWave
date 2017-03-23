package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.TilePosition
import Utilities.EnrichPosition._

object TileHeuristicEnemyAtMaxRange extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
    val us = intent.unit
    val kiteableEnemies = intent.threats.filter(isKiteable(intent.unit, _))
    
    if (intent.threats.isEmpty) return 1.0
    
    val nearestThreat = intent.threats.minBy(effectiveDistance(us, _))
    
    if ( ! isKiteable(us, nearestThreat)) return 1.0
    
    //Evaluate the absolute difference between our range and the enemy's distance from the candidate
    Math.max(1.0, 1.0 / Math.abs(us.unitClass.maxAirGroundRange - nearestThreat.pixelDistance(candidate.pixelCenter)))
  }
  
  def isKiteable(us:UnitInfo, enemy: UnitInfo):Boolean = {
    enemy.unitClass.maxAirGroundRange < us.unitClass.maxAirGroundRange
  }
  
  def effectiveDistance(us:UnitInfo, enemy:UnitInfo):Double = {
    enemy.pixelDistance(us) - enemy.unitClass.maxAirGroundRange
  }
  
}
