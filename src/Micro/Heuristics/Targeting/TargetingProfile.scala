package Micro.Heuristics.Targeting

class TargetingProfile(
  var preferVpfOurs     : Double = 0.0,
  var preferDetectors   : Double = 0.0,
  var avoidDelay        : Double = 0.0,
  var avoidPain         : Double = 0.0) {
  
  def weightedHeuristics: Iterable[TargetHeuristicWeight] = {
    Vector(
      new TargetHeuristicWeight(TargetHeuristicVpfOurs,       preferVpfOurs),
      new TargetHeuristicWeight(TargetHeuristicDetectors,     preferDetectors),
      new TargetHeuristicWeight(TargetHeuristicDelay,         -avoidDelay),
      new TargetHeuristicWeight(TargetHeuristicPain,          -avoidPain)
    )
  }
}
