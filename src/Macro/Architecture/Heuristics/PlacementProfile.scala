package Macro.Architecture.Heuristics

import Debugging.Visualizations.Colors


class PlacementProfile(
  val name                        : String,
  var preferZone                  : Double = 0.0,
  var preferNatural               : Double = 0.0,
  var preferGas                   : Double = 0.0,
  var preferSpace                 : Double = 0.0,
  var preferPowering              : Double = 0.0,
  var preferDistanceFromEnemy     : Double = 0.0,
  var preferCoveringWorkers       : Double = 0.0,
  var avoidDistanceFromBase       : Double = 0.0,
  var avoidDistanceFromExitRange  : Double = 0.0,
  var avoidSurfaceArea            : Double = 0.0) {
  
  def weightedHeuristics: Iterable[PlacementHeuristicWeight] = {
    Vector(
      new PlacementHeuristicWeight(PlacementHeuristicZone,                  preferZone,                   Colors.MediumRed),
      new PlacementHeuristicWeight(PlacementHeuristicNatural,               preferNatural,                Colors.NeonRed),
      new PlacementHeuristicWeight(PlacementHeuristicGas,                   preferGas,                    Colors.NeonOrange),
      new PlacementHeuristicWeight(PlacementHeuristicSpace,                 preferSpace,                  Colors.NeonYellow),
      new PlacementHeuristicWeight(PlacementHeuristicPowering,              preferPowering,               Colors.NeonGreen),
      new PlacementHeuristicWeight(PlacementHeuristicDistanceFromEnemy,     preferDistanceFromEnemy,      Colors.NeonTeal),
      new PlacementHeuristicWeight(PlacementHeuristicDistanceFromBase,      -avoidDistanceFromBase,       Colors.NeonBlue),
      new PlacementHeuristicWeight(PlacementHeuristicDistanceFromExitRange, -avoidDistanceFromExitRange,  Colors.NeonIndigo),
      new PlacementHeuristicWeight(PlacementHeuristicSurfaceArea,           -avoidSurfaceArea,            Colors.NeonViolet)
    )
  }
  
  override def toString: String = name
}
