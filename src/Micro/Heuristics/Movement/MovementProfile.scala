package Micro.Heuristics.Movement

import Debugging.Visualizations.Colors

class MovementProfile(
  var preferDestination     : Double = 0,
  var preferOrigin          : Double = 0,
  var preferThreatDistance  : Double = 0,
  var preferTarget          : Double = 0,
  var preferTargetValue     : Double = 0,
  var preferMobility        : Double = 0,
  var avoidExplosions       : Double = 0,
  var avoidDamage           : Double = 0,
  var avoidTraffic          : Double = 0) {
  
  def weightedHeuristics: Iterable[MovementHeuristicWeight] =
    Vector(
      new MovementHeuristicWeight(MovementHeuristicDestination,             preferDestination,    Colors.NeonViolet),
      new MovementHeuristicWeight(MovementHeuristicOrigin,                  preferOrigin,         Colors.NeonGreen),
      new MovementHeuristicWeight(MovementHeuristicThreatDistance,          preferThreatDistance, Colors.NeonOrange),
      new MovementHeuristicWeight(MovementHeuristicTargetInRange,           preferTarget,         Colors.NeonBlue),
      new MovementHeuristicWeight(MovementHeuristicTargetValue,             preferTargetValue,    Colors.NeonBlue),
      new MovementHeuristicWeight(MovementHeuristicMobility,                preferMobility,       Colors.NeonTeal),
      new MovementHeuristicWeight(MovementHeuristicExplosions,              -avoidExplosions,     Colors.NeonYellow),
      new MovementHeuristicWeight(MovementHeuristicExposureToDamage,        -avoidDamage,         Colors.NeonRed),
      new MovementHeuristicWeight(MovementHeuristicTraffic,                 -avoidTraffic,        Colors.NeonYellow)
    )
  
  def combine(other:MovementProfile) {
    preferDestination     += other.preferDestination
    preferOrigin          += other.preferOrigin
    preferThreatDistance  += other.preferThreatDistance
    preferTarget          += other.preferTarget
    preferTargetValue     += other.preferTargetValue
    preferMobility        += other.preferMobility
    avoidDamage           += other.avoidDamage
    avoidTraffic          += other.avoidTraffic
  }
}
