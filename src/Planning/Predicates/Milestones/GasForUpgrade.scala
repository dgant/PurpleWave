package Planning.Predicates.Milestones

import Planning.Plans.Compound.Or
import Planning.Predicates.Economy.GasAtLeast
import ProxyBwapi.Upgrades.Upgrade

class GasForUpgrade(upgrade: Upgrade, level: Int = 1) extends Or(
  new GasAtLeast(upgrade.gasPrice(level)),
  new UpgradeStarted(upgrade, level))
