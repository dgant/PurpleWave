package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterExposed extends TargetFilter {

  override def appliesTo(actor: FriendlyUnitInfo): Boolean = true

  // Avoid eating static anti-air shots unless necessary
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    target.tile.enemyRangeAgainst(actor) == 0
  }
}
