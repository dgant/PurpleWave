package Macro.BuildRequests

import Macro.Buildables.BuildableTech

case class Tech(tech: ProxyBwapi.Techs.Tech) extends BuildRequest(BuildableTech(tech))
