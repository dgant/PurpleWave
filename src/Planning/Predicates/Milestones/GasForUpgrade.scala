package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}
import ProxyBwapi.Upgrades.Upgrade

case class GasForUpgrade(upgrade: Upgrade, level: Int = 1) extends Predicate {
  override def apply: Boolean = MacroFacts.haveGasForUpgrade(upgrade, level)
}