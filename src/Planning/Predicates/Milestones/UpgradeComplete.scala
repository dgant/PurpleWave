package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate
import ProxyBwapi.Upgrades.Upgrade

case class UpgradeComplete(upgrade: Upgrade, level: Int = 1, withinFrames: Int = 0) extends Predicate {
  override def apply: Boolean = MacroFacts.upgradeComplete(upgrade, level, withinFrames)
}
