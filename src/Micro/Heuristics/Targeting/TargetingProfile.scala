package Micro.Heuristics.Targeting

class TargetingProfile(
  var preferSame        : Double = 0.0,
  var preferInRange     : Double = 0.0,
  var preferValue       : Double = 0.0,
  var preferCombat      : Double = 0.0,
  var preferDps         : Double = 0.0,
  var preferDamageType  : Double = 0.0,
  var avoidHealth       : Double = 0.0,
  var avoidDistance     : Double = 0.0,
  var avoidDistraction  : Double = 0.0) {
  
  def weightedHeuristics: Iterable[TargetHeuristicWeight] = {
    Vector(
      new TargetHeuristicWeight(TargetHeuristicSame,            preferSame),
      new TargetHeuristicWeight(TargetHeuristicInRange,         preferInRange),
      new TargetHeuristicWeight(TargetHeuristicValue,           preferValue),
      new TargetHeuristicWeight(TargetHeuristicCombat,          preferCombat),
      new TargetHeuristicWeight(TargetHeuristicDamagePerSecond, preferDps),
      new TargetHeuristicWeight(TargetHeuristicDamageType,      preferDamageType),
      new TargetHeuristicWeight(TargetHeuristicHealth,          -avoidHealth),
      new TargetHeuristicWeight(TargetHeuristicDistance,        -avoidDistance),
      new TargetHeuristicWeight(TargetHeuristicDistraction,     -avoidDistraction)
    )
  }
}
