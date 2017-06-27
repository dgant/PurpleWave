package Macro.Architecture.Heuristics

import Debugging.Visualizations.Colors


class PlacementProfile(
  val name                : String,
  var preferZone          : Double = 0.0,
  var preferGas           : Double = 0.0,
  var preferSpace         : Double = 0.0,
  var preferPowering      : Double = 0.0,
  var preferEnemyDistance : Double = 0.0,
  var avoidDistance       : Double = 0.0,
  var avoidExitDistance   : Double = 0.0
  ) {
  
  def weightedHeuristics: Iterable[PlacementHeuristicWeight] = {
    Vector(
      new PlacementHeuristicWeight(PlacementHeuristicZone,          preferZone,           Colors.NeonRed),
      new PlacementHeuristicWeight(PlacementHeuristicGas,           preferGas,            Colors.NeonYellow),
      new PlacementHeuristicWeight(PlacementHeuristicSpace,         preferSpace,          Colors.NeonGreen),
      new PlacementHeuristicWeight(PlacementHeuristicPowering,      preferPowering,       Colors.NeonTeal),
      new PlacementHeuristicWeight(PlacementHeuristicEnemy,         preferEnemyDistance,  Colors.NeonIndigo),
      new PlacementHeuristicWeight(PlacementHeuristicDistance,     -avoidDistance,        Colors.NeonBlue),
      new PlacementHeuristicWeight(PlacementHeuristicExit,         -avoidExitDistance,    Colors.NeonOrange)
    )
  }
  
  override def toString: String = name
}
