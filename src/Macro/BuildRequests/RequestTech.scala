package Macro.BuildRequests

import Macro.Buildables.BuildableTech
import bwapi.TechType

case class RequestTech(techType: TechType) extends BuildRequest(new BuildableTech(techType))
