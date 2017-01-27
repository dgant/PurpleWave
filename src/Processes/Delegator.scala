package Processes

import Types.Decisions.Decision
import Types.Tactics.Tactic

class Delegator {
  def delegateTactics(decisions:Iterable[Decision]): Iterable[Tactic] = {
    decisions.flatMap(decision => decision.plan.execute())
  }
}
