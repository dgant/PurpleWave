package Planning.Predicates.Milestones

import ProxyBwapi.Techs.Tech

class TechStarted(tech: Tech, level: Int = 1) extends TechComplete(tech, tech.researchFrames)