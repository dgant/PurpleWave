package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention
import Utilities.Enrichment.EnrichPosition._
import bwapi.TilePosition

object Dodge extends Command {
  
  def execute(intent:Intention) {
  
    val unit = intent.unit
    
    if (intent.battle.isEmpty) {
      Flee.execute(intent)
      return
    }
  
    val currentPosition = unit.tilePosition
    val kitePositions =
      (-3 to 3).flatten(dy =>
        (-3 to 3).map(dx => (dx, dy)))
        .filter(point => point._1 != 0 || point._2 != 0)
        .map(point => currentPosition.add(point._1, point._2))
        .filter(tile => With.grids.walkability.get(tile) > 0)
    
    if (kitePositions.nonEmpty) {
      val kitePosition = kitePositions.maxBy(kitePositionOption => _evaluatePosition(currentPosition, kitePositionOption))
      With.commander.move(this, unit, kitePosition.centerPosition)
    }
  }
  
  def _evaluatePosition(currentPosition:TilePosition, kitePosition:TilePosition):Double = {
    val distanceHomeCurrent = With.paths.getGroundDistance(currentPosition, With.geography.home.toTilePosition)
    val distanceHomeKiting  = With.paths.getGroundDistance(kitePosition, With.geography.home.toTilePosition)
    val distanceBonus = if (distanceHomeKiting < distanceHomeCurrent) 3 else 1
    val mobility = With.grids.mobility.get(kitePosition)
    val threat = With.grids.enemyGroundStrength.get(kitePosition)
    val evaluation = distanceBonus * mobility / (1.0 + threat)
    evaluation
  }
}
