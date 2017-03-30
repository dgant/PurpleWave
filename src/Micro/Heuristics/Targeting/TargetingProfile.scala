package Micro.Heuristics.Targeting
import Micro.Heuristics.UnitHeuristics._

class TargetingProfile(
  var preferInRange     : Double = 0,
  var preferValue       : Double = 0,
  var preferCombat      : Double = 0,
  var preferDps         : Double = 0,
  var avoidHealth       : Double = 0,
  var avoidDistance     : Double = 0,
  var avoidDistraction  : Double = 0) {
  
  def weightedHeuristics: Iterable[WeightedUnitHeuristic] = {
    List(
      new WeightedUnitHeuristic(UnitHeuristicInRange,         preferInRange),
      new WeightedUnitHeuristic(UnitHeuristicValue,           preferValue),
      new WeightedUnitHeuristic(UnitHeuristicCombat,          preferCombat),
      new WeightedUnitHeuristic(UnitHeuristicDamagePerSecond, preferDps),
      new WeightedUnitHeuristic(UnitHeuristicHealth,          -avoidHealth),
      new WeightedUnitHeuristic(UnitHeuristicDistance,        -avoidDistance),
      new WeightedUnitHeuristic(UnitHeuristicDistraction,     -avoidDistraction)
      //new WeightedUnitHeuristic(UnitHeuristicFiringPosition,  preferDps),
    )
  }
}
