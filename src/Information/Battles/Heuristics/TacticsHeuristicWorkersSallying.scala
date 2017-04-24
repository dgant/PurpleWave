package Information.Battles.Heuristics

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath

object TacticsHeuristicWorkersSallying extends TacticsHeuristic {
  
  override def evaluate(context: Battle, candidate: TacticsOptions): Double = {
    
    HeuristicMath.fromBoolean(
      (
        candidate.has(Tactics.Workers.FightAll) ||
        candidate.has(Tactics.Workers.FightHalf)
      )
      && ! context.enemy.vanguard.zone.bases.exists(_.owner == With.self))
  }
}
