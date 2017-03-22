package Micro.Heuristics.Movement

import Micro.Heuristics.TileHeuristics._
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
      new WeightedTileHeuristic(TileHeuristicEnemyAtMaxRange,   preferSitAtRange,   Color.Orange),
      new WeightedTileHeuristic(TileHeuristicMobility,          preferMobility,     Color.Green),
      new WeightedTileHeuristic(TileHeuristicHighGround,        preferHighGround,   Color.Cyan),
      new WeightedTileHeuristic(TileHeuristicRegrouping,        preferGrouping,     Color.Purple),
      new WeightedTileHeuristic(TileHeuristicKeepMoving,        preferMoving,       Color.Brown),
      new WeightedTileHeuristic(TileHeuristicRandom,            preferRandom,       Color.Yellow),
      new WeightedTileHeuristic(TileHeuristicExposureToDamage,  -avoidDamage,       Color.Red),
      new WeightedTileHeuristic(TileHeuristicTraffic,           -avoidTraffic,      Color.Blue),
      new WeightedTileHeuristic(TileHeuristicEnemyVision,       -avoidVision,       Color.Teal),
      new WeightedTileHeuristic(TileHeuristicEnemyDetection,    -avoidDetection,    Color.White)
    )
    .map(_.weigh(intent, candidate))
    .product
}
