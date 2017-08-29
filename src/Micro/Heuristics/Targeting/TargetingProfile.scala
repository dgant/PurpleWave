package Micro.Heuristics.Targeting

class TargetingProfile(
  var preferVpfEnemy    : Double = 0.0,
  var preferVpfOurs     : Double = 0.0,
  var preferDetectors   : Double = 0.0,
  var preferFocusFire   : Double = 0.0,
  var avoidPain         : Double = 0.0,
  var avoidDelay        : Double = 0.0) {
  
  def weightedHeuristics: Iterable[TargetHeuristicWeight] = {
    Vector(
      new TargetHeuristicWeight(TargetHeuristicVpfEnemy,    preferVpfEnemy),
      new TargetHeuristicWeight(TargetHeuristicVpfOurs,     preferVpfOurs),
      new TargetHeuristicWeight(TargetHeuristicDetectors,   preferDetectors),
      new TargetHeuristicWeight(TargetHeuristicFocusFire,   preferFocusFire),
      new TargetHeuristicWeight(TargetHeuristicPain,        -avoidPain),
      new TargetHeuristicWeight(TargetHeuristicDelay,       -avoidDelay)
    )
  }
}
