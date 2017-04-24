package Information.Battles.Heuristics

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Mathematics.Heuristics.{Heuristic, HeuristicResult}

class TacticsHeuristicResult (
  heuristic   : Heuristic[Battle, TacticsOptions],
  battle      : Battle,
  tactics     : TacticsOptions,
  evaluation  : Double)
  
  extends HeuristicResult(
    heuristic,
    battle,
    tactics,
    evaluation)
