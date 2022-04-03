package Macro.Buildables

import ProxyBwapi.Techs.Tech

case class RequestTech(techType: Tech) extends RequestProduction(techType)
