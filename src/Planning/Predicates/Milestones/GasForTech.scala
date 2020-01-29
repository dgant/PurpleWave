package Planning.Predicates.Milestones

import Planning.Plans.Compound.Or
import Planning.Predicates.Economy.GasAtLeast
import ProxyBwapi.Techs.Tech

class GasForTech(tech: Tech) extends Or(
  new GasAtLeast(tech.gasPrice),
  new TechStarted(tech))
