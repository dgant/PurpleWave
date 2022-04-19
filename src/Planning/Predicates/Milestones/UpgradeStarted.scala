package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}
import ProxyBwapi.Upgrades.Upgrade

case class UpgradeStarted(upgrade: Upgrade, level: Int = 1) extends Predicate {
  override def apply: Boolean = MacroFacts.upgradeStarted(upgrade, level)
}