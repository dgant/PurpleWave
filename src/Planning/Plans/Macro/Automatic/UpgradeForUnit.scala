package Planning.Plans.Macro.Automatic

import Macro.BuildRequests.RequestUpgrade
import Planning.Composition.UnitMatchers.UnitMatcher
import Planning.Plans.Compound.If
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Upgrades.Upgrade

class UpgradeForUnit(quantity: Int, unit: UnitMatcher, upgrade: Upgrade, level: Int = 1) extends If(
  new UnitsAtLeast(quantity, unit),
  new Build(RequestUpgrade(upgrade, level)))
