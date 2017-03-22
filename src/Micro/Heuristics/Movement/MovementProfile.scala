package Micro.Heuristics.Movement

import Micro.Heuristics.HeuristicMath
import Micro.Heuristics.TileHeuristics.{TileHeuristic, TileHeuristicDestinationHere, TileHeuristicDestinationNearby}
import Micro.Intentions.Intention
import bwapi.TilePosition

private class WeightedTileHeuristic(val heuristic: TileHeuristic, val weight:Double) {
  
  def weigh(intent:Intention, candidate:TilePosition):Double = {
    if (weight == 0)
      1
    else
      Math.pow(HeuristicMath.normalize(heuristic.evaluate(intent, candidate)), weight)
  }
  
}

class MovementProfile(
  var preferTravel      : Double = 0,
  var preferSpot        : Double = 0,
  var preferSitAtRange  : Double = 0,
  var preferMobility    : Double = 0,
  var preferHighGround  : Double = 0,
  var preferGrouping    : Double = 0,
  var preferMoving      : Double = 0,
  var preferRandom      : Double = 0,
  var avoidDamage       : Double = 0,
  var avoidTraffic      : Double = 0,
  var avoidVision       : Double = 0,
  var avoidDetection    : Double = 0) {
  
  def evaluate(intent: Intention, candidate: TilePosition): Double =
    List(
      new WeightedTileHeuristic(TileHeuristicDestinationNearby, preferTravel),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferSpot),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferSitAtRange),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferMobility),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferHighGround),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferGrouping),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferMoving),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferRandom),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   -avoidDamage),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   -avoidTraffic),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   -avoidVision),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   -avoidDetection)
    )
    .map(_.weigh(intent, candidate))
    .product
}
