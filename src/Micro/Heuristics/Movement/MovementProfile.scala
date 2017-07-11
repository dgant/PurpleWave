package Micro.Heuristics.Movement

import Debugging.Visualizations.Colors

case class MovementProfile(
  var preferDestination     : Double = 0,
  var preferOrigin          : Double = 0,
  var preferThreatDistance  : Double = 0,
  var preferTarget          : Double = 0,
  var preferTargetValue     : Double = 0,
  var preferMobility        : Double = 0,
  var avoidExplosions       : Double = 0,
  var avoidDamage           : Double = 0,
  var avoidTraffic          : Double = 0,
  var avoidShovers          : Double = 0) {
  
  def this(source: MovementProfile) {
    this(
      source.preferDestination,
      source.preferOrigin,
      source.preferThreatDistance,
      source.preferTarget,
      source.preferTargetValue,
      source.preferMobility,
      source.avoidExplosions,
      source.avoidDamage,
      source.avoidTraffic,
      source.avoidShovers)
  }
    
  def weightedHeuristics: Iterable[MovementHeuristicWeight] =
    Vector(
      new MovementHeuristicWeight(MovementHeuristicDestination,             preferDestination,    Colors.NeonViolet),
      new MovementHeuristicWeight(MovementHeuristicOrigin,                  preferOrigin,         Colors.NeonGreen),
      new MovementHeuristicWeight(MovementHeuristicThreatDistance,          preferThreatDistance, Colors.NeonIndigo),
      new MovementHeuristicWeight(MovementHeuristicTargetInRange,           preferTarget,         Colors.NeonOrange),
      new MovementHeuristicWeight(MovementHeuristicTargetValue,             preferTargetValue,    Colors.NeonOrange),
      new MovementHeuristicWeight(MovementHeuristicMobility,                preferMobility,       Colors.BrightGray),
      new MovementHeuristicWeight(MovementHeuristicExplosions,              -avoidExplosions,     Colors.NeonYellow),
      new MovementHeuristicWeight(MovementHeuristicExposureToDamage,        -avoidDamage,         Colors.NeonRed),
      new MovementHeuristicWeight(MovementHeuristicTraffic,                 -avoidTraffic,        Colors.NeonTeal),
      new MovementHeuristicWeight(MovementHeuristicShovers,                 -avoidShovers,        Colors.NeonBlue)
    )
}
