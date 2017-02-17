package Types.Buildable

import bwapi.TechType

class BuildableTech(techType: TechType) extends Buildable(tech = Some(techType)){}
