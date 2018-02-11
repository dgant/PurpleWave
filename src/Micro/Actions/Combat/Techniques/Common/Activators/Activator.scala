package Micro.Actions.Combat.Techniques.Common.Activators

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

trait Activator {
  def apply(unit: FriendlyUnitInfo, others: Seq[UnitInfo]): Option[Double]
}
