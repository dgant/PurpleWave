package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor
import Mathematics.Points.Tile

object EvaluatePlacements {
  
  def best(buildingDescriptor: BuildingDescriptor, tiles: Iterable[Tile]): Option[Tile] = {
    
    if (tiles.isEmpty) return None
    
    Some(tiles.maxBy(target => evaluate(buildingDescriptor, target)))
  }
  
  def evaluate(buildingDescriptor: BuildingDescriptor, target: Tile): Double = {
    buildingDescriptor.placement.weightedHeuristics.map(_.weighMultiplicatively(buildingDescriptor, target)).product
  }
}
