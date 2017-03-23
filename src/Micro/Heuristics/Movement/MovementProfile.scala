package Micro.Heuristics.Movement

import Micro.Heuristics.TileHeuristics._
import bwapi.Color

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
  
  def heuristics: Iterable[WeightedMovementHeuristic] =
    List(
      new WeightedMovementHeuristic(TileHeuristicDestinationNearby, preferTravel,       Color.Brown),
      new WeightedMovementHeuristic(TileHeuristicDestinationHere,   preferSpot,         Color.Black),
      new WeightedMovementHeuristic(TileHeuristicEnemyAtMaxRange,   preferSitAtRange,   Color.Orange),
      new WeightedMovementHeuristic(TileHeuristicMobility,          preferMobility,     Color.Green),
      new WeightedMovementHeuristic(TileHeuristicHighGround,        preferHighGround,   Color.Cyan),
      new WeightedMovementHeuristic(TileHeuristicRegrouping,        preferGrouping,     Color.Purple),
      new WeightedMovementHeuristic(TileHeuristicKeepMoving,        preferMoving,       Color.Blue),
      new WeightedMovementHeuristic(TileHeuristicRandom,            preferRandom,       Color.Grey),
      new WeightedMovementHeuristic(TileHeuristicExposureToDamage,  -avoidDamage,       Color.Red),
      new WeightedMovementHeuristic(TileHeuristicTraffic,           -avoidTraffic,      Color.Orange),
      new WeightedMovementHeuristic(TileHeuristicEnemyVision,       -avoidVision,       Color.Teal),
      new WeightedMovementHeuristic(TileHeuristicEnemyDetection,    -avoidDetection,    Color.White)
    )
}
