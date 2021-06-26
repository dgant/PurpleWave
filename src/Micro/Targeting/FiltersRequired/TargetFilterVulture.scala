package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Minutes

object TargetFilterVulture extends TargetFilter {
  simulationSafe = true
  private val cutoff = Minutes(4)()
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = Terran.Vulture(actor) && With.frame < cutoff
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    target.unitClass.canAttack || ! target.unitClass.isBuilding
  }
}
