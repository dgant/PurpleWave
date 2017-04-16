package Micro.Heuristics.MovementHeuristics

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.Tile
import Micro.Intent.Intention
object MovementHeuristicTraffic extends MovementHeuristic {
  
  val scaling = 1.0 / 32.0 / 32.0
  
  override def evaluate(intent: Intention, candidate: Tile): Double = {
  
    if (intent.unit.flying) return HeuristicMath.default
    
    Vector(
      measureTraffic(intent, 1.00, candidate),
      measureTraffic(intent, 0.75, candidate.add(-1,  0)),
      measureTraffic(intent, 0.75, candidate.add( 1,  0)),
      measureTraffic(intent, 0.75, candidate.add( 0, -1)),
      measureTraffic(intent, 0.75, candidate.add( 0, -1)),
      measureTraffic(intent, 0.50, candidate.add(-1, -1)),
      measureTraffic(intent, 0.50, candidate.add( 1, -1)),
      measureTraffic(intent, 0.50, candidate.add(-1,  1)),
      measureTraffic(intent, 0.50, candidate.add( 1,  1))
    ).sum
  }
  
  def measureTraffic(
    intent:Intention,
    multiplier:Double,
    candidate:Tile)
  :Double = {
    
    multiplier *
    scaling *
    With.grids.units.get(candidate)
    .filter(neighbor =>
      neighbor.possiblyStillThere
      && neighbor != intent.unit
      && ! neighbor.flying
      && ! neighbor.unitClass.isBuilding)
    .map(neighbor =>
      Math.min(32.0, neighbor.unitClass.width) *
      Math.min(32.0, neighbor.unitClass.height))
    .sum
  }
  
}
