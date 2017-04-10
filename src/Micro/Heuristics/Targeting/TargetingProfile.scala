package Micro.Heuristics.Targeting
import Micro.Heuristics.TargetHeuristics._

class TargetingProfile(
  var preferInRange     : Double = 0,
  var preferValue       : Double = 0,
  var preferCombat      : Double = 0,
  var preferDps         : Double = 0,
  var avoidHealth       : Double = 0,
  var avoidDistance     : Double = 0,
  var avoidDistraction  : Double = 0) {
  
  def weightedHeuristics: Iterable[TargetHeuristicWeight] = {
    List(
      new TargetHeuristicWeight(TargetHeuristicInRange,         preferInRange),
      new TargetHeuristicWeight(TargetHeuristicValue,           preferValue),
      new TargetHeuristicWeight(TargetHeuristicCombat,          preferCombat),
      new TargetHeuristicWeight(TargetHeuristicDamagePerSecond, preferDps),
      new TargetHeuristicWeight(TargetHeuristicHealth,          -avoidHealth),
      new TargetHeuristicWeight(TargetHeuristicDistance,        -avoidDistance),
      new TargetHeuristicWeight(TargetHeuristicDistraction,     -avoidDistraction)
      //new TargetHeuristicWeight(UnitHeuristicFiringPosition,  preferDps),
    )
  }
  
  def combined(other:TargetingProfile) {
    preferInRange     += other.preferInRange
    preferValue       += other.preferValue
    preferCombat      += other.preferCombat
    preferDps         += other.preferDps
    avoidHealth       += other.avoidHealth
    avoidDistance     += other.avoidDistance
    avoidDistraction  += other.avoidDistraction
  }
}
