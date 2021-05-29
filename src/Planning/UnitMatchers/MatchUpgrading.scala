package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.Upgrade

case class MatchUpgrading(upgrades: Upgrade*) extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean =
    unit.upgrading && (upgrades.isEmpty || upgrades.exists(unit.upgradeProducing.contains))
}
