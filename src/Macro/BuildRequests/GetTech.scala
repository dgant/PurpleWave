package Macro.BuildRequests

import Macro.Buildables.BuildableTech

case class GetTech(tech: ProxyBwapi.Techs.Tech) extends BuildRequest(BuildableTech(tech))
