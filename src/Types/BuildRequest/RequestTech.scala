package Types.BuildRequest

import Types.Buildable.BuildableTech
import bwapi.TechType

case class RequestTech(techType: TechType) extends BuildRequest(new BuildableTech(techType))
