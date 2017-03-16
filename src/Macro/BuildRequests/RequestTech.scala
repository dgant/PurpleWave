package Macro.BuildRequests

import Macro.Buildables.BuildableTech
import ProxyBwapi.Techs.Tech

case class RequestTech(tech: Tech) extends BuildRequest(new BuildableTech(tech))
