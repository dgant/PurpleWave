package Planning.Predicates.Milestones

import ProxyBwapi.Upgrades.Upgrade

class UpgradeStarted(upgrade: Upgrade, level: Int = 1) extends UpgradeComplete(upgrade, level, upgrade.upgradeFrames(level))