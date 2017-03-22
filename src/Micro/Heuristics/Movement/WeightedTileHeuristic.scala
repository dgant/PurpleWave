package Micro.Heuristics.Movement


import Debugging.Visualization.VisualizeTileHeuristics
import Micro.Heuristics.HeuristicMath
import Micro.Heuristics.TileHeuristics.TileHeuristic
import Micro.Intentions.Intention
import bwapi.{Color, TilePosition}

class WeightedTileHeuristic(
  val heuristic : TileHeuristic,
  val weight    : Double,
  val color     : Color = Color.Grey) {
  
  def weigh(intent:Intention, candidate:TilePosition):Double = {
    
    val result =
      if (weight == 0)
        1.0
      else
        Math.pow(HeuristicMath.normalize(heuristic.evaluate(intent, candidate)), weight)
  
    if (intent.unit.selected) {
      VisualizeTileHeuristics.render(intent, candidate, result, color)
    }
    
    return result
  }
  
}
