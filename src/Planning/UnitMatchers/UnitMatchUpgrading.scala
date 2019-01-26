package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.Upgrade

case class UnitMatchUpgrading(upgrade: Upgrade  = null) extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean =
    unit.upgrading && (upgrade == null || unit.upgradeProducing.contains(upgrade))
}
