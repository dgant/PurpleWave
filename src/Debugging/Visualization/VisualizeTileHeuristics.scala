package Debugging.Visualization

import Micro.Heuristics.HeuristicMath
import Micro.Intentions.Intention
import Startup.With
import Utilities.EnrichPosition._
import bwapi.{Color, TilePosition}

object VisualizeTileHeuristics {
  
  var magnification = 10.0
  val denominator = Math.log(HeuristicMath.heuristicMaximum)
  
  def render(intent:Intention, candidate:TilePosition, value:Double, color:Color) {
    if (  ! With.configuration.enableVisualization ||
          ! With.configuration.enableVisualizationTileHeuristics) return
    
    val absoluteWeightedValue = if (value >= 1.0) value else 1.0 / value
    
    if (value <= 1.0) {
      return
    }
    
    val center = intent.unit.tileCenter
    val circleRadius = 32.0 * magnification * Math.log(absoluteWeightedValue) / denominator
    val lineLength = center.pixelCenter.distancePixels(candidate.pixelCenter) - circleRadius
    
    DrawMap.circle(candidate.toPosition, circleRadius.toInt, color)
    //DrawMap.line(candidate.toPosition, )
  }
}
