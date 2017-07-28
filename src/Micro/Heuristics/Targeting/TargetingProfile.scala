package Micro.Heuristics.Targeting

class TargetingProfile(
  var preferInRange         : Double = 0.0,
  var preferValue           : Double = 0.0,
  var preferCombat          : Double = 0.0,
  var preferDpf             : Double = 0.0,
  var preferDamageAgainst   : Double = 0.0,
  var avoidPain             : Double = 1.0,
  var avoidHealth           : Double = 0.0,
  var avoidDistance         : Double = 0.0,
  var avoidDistraction      : Double = 0.0) {
  
  def weightedHeuristics: Iterable[TargetHeuristicWeight] = {
    Vector(
      new TargetHeuristicWeight(TargetHeuristicInRange,         preferInRange),
      new TargetHeuristicWeight(TargetHeuristicValue,           preferValue),
      new TargetHeuristicWeight(TargetHeuristicCombat,          preferCombat),
      new TargetHeuristicWeight(TargetHeuristicDamagePerSecond, preferDpf),
      new TargetHeuristicWeight(TargetHeuristicDpfAgainst,   preferDamageAgainst),
      new TargetHeuristicWeight(TargetHeuristicPain,            -avoidPain),
      new TargetHeuristicWeight(TargetHeuristicHealth,          -avoidHealth),
      new TargetHeuristicWeight(TargetHeuristicDistance,        -avoidDistance),
      new TargetHeuristicWeight(TargetHeuristicDistraction,     -avoidDistraction)
    )
  }
}
