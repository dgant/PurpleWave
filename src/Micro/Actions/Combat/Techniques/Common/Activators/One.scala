package Micro.Actions.Combat.Techniques.Common.Activators

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object One extends Activator {
  override def apply(unit: FriendlyUnitInfo, others: Seq[UnitInfo]): Option[Double] = Some(1.0)
}
