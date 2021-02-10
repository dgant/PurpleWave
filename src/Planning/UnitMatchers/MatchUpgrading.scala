package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.Upgrade

case class MatchUpgrading(upgrade: Upgrade  = null) extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean =
    unit.upgrading && (upgrade == null || unit.upgradeProducing.contains(upgrade))
}
