package Types.Plans

import Types.Resources.JobDescription
import Types.Tactics.Tactic

abstract class Plan {
  def execute():Iterable[Tactic]
}