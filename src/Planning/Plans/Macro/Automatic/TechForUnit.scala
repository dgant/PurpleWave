package Planning.Plans.Macro.Automatic

import Macro.BuildRequests.Get
import Planning.Composition.UnitMatchers.UnitMatcher
import Planning.Plans.Compound.If
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Milestones.UnitsAtLeast

class TechForUnit(quantity: Int, unit: UnitMatcher, tech: ProxyBwapi.Techs.Tech) extends If(
  new UnitsAtLeast(quantity, unit),
  new Build(Get(tech)))
