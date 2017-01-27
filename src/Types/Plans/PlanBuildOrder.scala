package Types.Plans

import Types.Resources.Resource
import Types.Tactics.Tactic

class PlanBuildOrder extends Plan {
  override def execute(): Iterable[Tactic] = {
    throw new Exception
  }

  override def getRequiredResources(): Resource = {
    throw new Exception
  }
}
