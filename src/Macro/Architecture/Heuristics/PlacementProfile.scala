package Macro.Architecture.Heuristics

import Debugging.Visualizations.Colors


class PlacementProfile(
  val name                        : String,
  var preferZone                  : Double = 0.0,
  var preferNatural               : Double = 0.0,
  var preferResources             : Double = 0.0,
  var preferSpace                 : Double = 0.0,
  var preferPowering              : Double = 0.0,
  var preferDistanceFromEnemy     : Double = 0.0,
  var preferCoveringWorkers       : Double = 0.0,
  var preferSurfaceArea           : Double = 0.0,
  var avoidDistanceFromBase       : Double = 0.0,
  var avoidDistanceFromEnemy      : Double = 0.0,
  var avoidDistanceFromIdealRange : Double = 0.0,
  var avoidSurfaceArea            : Double = 0.0) {
  
  def this(name: String, other: PlacementProfile) {
    this (
      name,
      preferZone                  = other.preferZone,
      preferNatural               = other.preferNatural,
      preferResources             = other.preferResources,
      preferSpace                 = other.preferSpace,
      preferPowering              = other.preferPowering,
      preferDistanceFromEnemy     = other.preferDistanceFromEnemy,
      preferCoveringWorkers       = other.preferCoveringWorkers,
      preferSurfaceArea           = other.preferSurfaceArea,
      avoidDistanceFromBase       = other.avoidDistanceFromBase,
      avoidDistanceFromEnemy      = other.avoidDistanceFromEnemy,
      avoidDistanceFromIdealRange = other.avoidDistanceFromIdealRange,
      avoidSurfaceArea            = other.avoidSurfaceArea
    )
  }
  
  def weightedHeuristics: Iterable[PlacementHeuristicWeight] = {
    Vector(
      new PlacementHeuristicWeight(PlacementHeuristicZone,                    preferZone,                   Colors.MediumRed),
      new PlacementHeuristicWeight(PlacementHeuristicNatural,                 preferNatural,                Colors.NeonRed),
      new PlacementHeuristicWeight(PlacementHeuristicResources,               preferResources,              Colors.NeonOrange),
      new PlacementHeuristicWeight(PlacementHeuristicSpace,                   preferSpace,                  Colors.NeonYellow),
      new PlacementHeuristicWeight(PlacementHeuristicPowering,                preferPowering,               Colors.NeonGreen),
      new PlacementHeuristicWeight(PlacementHeuristicDistanceFromEnemy,       preferDistanceFromEnemy,      Colors.NeonTeal),
      new PlacementHeuristicWeight(PlacementHeuristicSurfaceArea,             preferSurfaceArea,            Colors.NeonViolet),
      new PlacementHeuristicWeight(PlacementHeuristicDistanceFromBase,        -avoidDistanceFromBase,       Colors.NeonBlue),
      new PlacementHeuristicWeight(PlacementHeuristicDistanceFromEnemy,       -avoidDistanceFromEnemy,      Colors.NeonBlue),
      new PlacementHeuristicWeight(PlacementHeuristicDistanceFromIdealRange,  -avoidDistanceFromIdealRange, Colors.NeonIndigo),
      new PlacementHeuristicWeight(PlacementHeuristicSurfaceArea,             -avoidSurfaceArea,            Colors.NeonViolet)
    )
  }
  
  override def toString: String = name
}
