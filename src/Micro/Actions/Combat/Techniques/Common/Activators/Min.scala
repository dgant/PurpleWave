package Micro.Actions.Combat.Techniques.Common.Activators

import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

class Min(technique: ActionTechnique) extends Activator {
  override def apply(unit: FriendlyUnitInfo, others: Seq[UnitInfo]): Option[Double] = {
    ByOption.min(others.flatMap(other => technique.applicabilityOther(unit, other)))
  }
}
