package Micro.Heuristics.Movement

import Debugging.Visualizations.Colors

case class MovementProfile(
  var preferVpfDealing      : Double = 0.0,
  var preferMobility        : Double = 0.0,
  var avoidVpfReceiving     : Double = 0.0,
  var avoidRetreatFrames    : Double = 0.0,
  var avoidDamage           : Double = 0.0,
  var avoidTraffic          : Double = 0.0,
  var avoidShovers          : Double = 0.0) {
  
  def this(source: MovementProfile) {
    this(
      source.preferVpfDealing,
      source.preferMobility,
      source.avoidVpfReceiving,
      source.avoidRetreatFrames,
      source.avoidDamage,
      source.avoidTraffic,
      source.avoidShovers)
  }
    
  def weightedHeuristics: Iterable[MovementHeuristicWeight] =
    Vector(
      new MovementHeuristicWeight(MovementHeuristicVpfDealing,              preferVpfDealing,     Colors.NeonViolet),
      new MovementHeuristicWeight(MovementHeuristicMobility,                preferMobility,       Colors.BrightGreen),
      new MovementHeuristicWeight(MovementHeuristicVpfReceiving,            -avoidVpfReceiving,   Colors.NeonOrange),
      new MovementHeuristicWeight(MovementHeuristicRetreatFrames,           -avoidRetreatFrames,  Colors.NeonRed),
      new MovementHeuristicWeight(MovementHeuristicExposureToDamage,        -avoidDamage,         Colors.NeonOrange),
      new MovementHeuristicWeight(MovementHeuristicTraffic,                 -avoidTraffic,        Colors.NeonTeal),
      new MovementHeuristicWeight(MovementHeuristicShovers,                 -avoidShovers,        Colors.NeonBlue)
    )
}
