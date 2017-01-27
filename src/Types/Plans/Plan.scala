package Types.Plans

import Types.Resources.Resource
import Types.Tactics.Tactic

abstract class Plan {
  def execute():Iterable[Tactic]
  def getRequiredResources():Resource
}