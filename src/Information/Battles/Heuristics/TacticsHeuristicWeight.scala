package Information.Battles.Heuristics

import Information.Battles.Types.{Battle, TacticsOptions}
import Mathematics.Heuristics.{Heuristic, HeuristicWeight}

class TacticsHeuristicWeight (
  heuristic : Heuristic[Battle, TacticsOptions],
  weight    : Double)

  extends HeuristicWeight(
    heuristic,
    weight)