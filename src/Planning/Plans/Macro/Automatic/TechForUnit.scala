package Planning.Plans.Macro.Automatic

import Macro.BuildRequests.RequestTech
import Planning.Composition.UnitMatchers.UnitMatcher
import Planning.Plans.Compound.If
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Techs.Tech

class TechForUnit(quantity: Int, unit: UnitMatcher, tech: Tech) extends If(
  new UnitsAtLeast(quantity, unit),
  new Build(RequestTech(tech)))
