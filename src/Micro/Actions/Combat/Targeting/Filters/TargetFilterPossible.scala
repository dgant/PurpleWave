package Micro.Actions.Combat.Targeting.Filters
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterPossible extends TargetFilter {
  simulationSafe = true
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = actor.canAttack(target)
}
