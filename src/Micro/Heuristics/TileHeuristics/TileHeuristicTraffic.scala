package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Startup.With
import bwapi.TilePosition
import Utilities.EnrichPosition._
object TileHeuristicTraffic extends TileHeuristic {
  
  val scaling = 1.0 / 32.0 / 32.0
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    if (intent.unit.flying) 1.0 else
      1.0 + scaling *
      List(
        measureTraffic(intent,   1.0, candidate),
        measureTraffic(intent,  0.25, candidate.add(-1,  0)),
        measureTraffic(intent,  0.25, candidate.add( 1,  0)),
        measureTraffic(intent,  0.25, candidate.add( 0, -1)),
        measureTraffic(intent,  0.25, candidate.add( 0,  1)),
        measureTraffic(intent, 0.125, candidate.add(-1, -1)),
        measureTraffic(intent, 0.125, candidate.add(-1,  1)),
        measureTraffic(intent, 0.125, candidate.add( 1, -1)),
        measureTraffic(intent, 0.125, candidate.add( 1,  1))
      ).sum
  }
  
  def measureTraffic(intent:Intention, multiplier:Double, tile:TilePosition):Double = {
    multiplier *
    With.grids.units.get(tile)
      .filterNot(_ == intent.unit)
      .filterNot(_.flying)
      .map(unit =>
        Math.max(32.0, unit.unitClass.width) *
        Math.max(32.0, unit.unitClass.height))
      .sum
  }
  
}
