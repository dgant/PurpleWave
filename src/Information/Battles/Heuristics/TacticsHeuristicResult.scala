package Information.Battles.Heuristics

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Mathematics.Heuristics.HeuristicResult

class TacticsHeuristicResult (
  heuristic   : TacticsHeuristic,
  battle      : Battle,
  tactics     : TacticsOptions,
  evaluation  : Double)
  
  extends HeuristicResult(
    heuristic,
    battle,
    tactics,
    evaluation)
