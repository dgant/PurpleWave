package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts
import ProxyBwapi.Upgrades.Upgrade

case class GasForUpgrade(upgrade: Upgrade, level: Int = 1) extends Predicate {
  override def apply: Boolean = MacroFacts.haveGasForUpgrade(upgrade, level)
}