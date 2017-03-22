package Micro.Heuristics.Movement

import Micro.Heuristics.TileHeuristics.{TileHeuristicDestinationHere, TileHeuristicDestinationNearby}
import Micro.Intentions.Intention
import bwapi.{Color, TilePosition}

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
      new WeightedTileHeuristic(TileHeuristicDestinationNearby, preferTravel,       Color.Grey),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferSpot,         Color.Black),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferSitAtRange,   Color.Orange),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferMobility,     Color.Green),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferHighGround,   Color.Cyan),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferGrouping,     Color.Purple),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferMoving,       Color.Brown),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   preferRandom,       Color.Yellow),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   -avoidDamage,       Color.Red),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   -avoidTraffic,      Color.Blue),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   -avoidVision,       Color.Teal),
      new WeightedTileHeuristic(TileHeuristicDestinationHere,   -avoidDetection,    Color.White)
    )
    .map(_.weigh(intent, candidate))
    .product
}
