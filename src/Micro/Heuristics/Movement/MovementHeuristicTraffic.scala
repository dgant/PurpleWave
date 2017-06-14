package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.{Pixel, Tile}
import Micro.Task.ExecutionState
object MovementHeuristicTraffic extends MovementHeuristic {
  
  val scaling = 1.0 / 32.0 / 32.0
  
  override def evaluate(state: ExecutionState, candidate: Pixel): Double = {
  
    if (state.unit.flying) return HeuristicMathMultiplicative.default
    
    val m0 = 1.00
    val m1 = 0.80
    val m2 = 0.70
    val m3 = 0.50
    
    val tile = candidate.tileIncluding
    
    Vector(
      measureTraffic(state, m0, tile),
      measureTraffic(state, m1, tile.add(-1,  0)),
      measureTraffic(state, m1, tile.add( 1,  0)),
      measureTraffic(state, m1, tile.add( 0, -1)),
      measureTraffic(state, m1, tile.add( 0, -1)),
      measureTraffic(state, m2, tile.add(-1, -1)),
      measureTraffic(state, m2, tile.add( 1, -1)),
      measureTraffic(state, m2, tile.add(-1,  1)),
      measureTraffic(state, m2, tile.add( 1,  1)),
      measureTraffic(state, m3, tile.add(-2, -1)),
      measureTraffic(state, m3, tile.add(-2,  0)),
      measureTraffic(state, m3, tile.add(-2,  1)),
      measureTraffic(state, m3, tile.add( 2, -1)),
      measureTraffic(state, m3, tile.add( 2,  0)),
      measureTraffic(state, m3, tile.add( 2,  1)),
      measureTraffic(state, m3, tile.add(-1, -2)),
      measureTraffic(state, m3, tile.add( 0, -2)),
      measureTraffic(state, m3, tile.add( 1, -2)),
      measureTraffic(state, m3, tile.add(-1,  2)),
      measureTraffic(state, m3, tile.add( 0,  2)),
      measureTraffic(state, m3, tile.add( 1,  2))
    ).sum
  }
  
  def measureTraffic(
    state       : ExecutionState,
    multiplier  : Double,
    candidate   : Tile)
      : Double = {
    
    multiplier *
    scaling *
    With.grids.units.get(candidate)
    .filter(neighbor =>
      neighbor.likelyStillThere
      && neighbor != state.unit
      && ! neighbor.flying
      && ! neighbor.unitClass.isBuilding)
    .map(neighbor =>
      Math.min(32.0, neighbor.unitClass.width) *
      Math.min(32.0, neighbor.unitClass.height))
    .sum
  }
  
}
