package Macro.Requests

import ProxyBwapi.Techs.Tech

case class RequestTech(techType: Tech) extends RequestBuildable(techType, 1)
