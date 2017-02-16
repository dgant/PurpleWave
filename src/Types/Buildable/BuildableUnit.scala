package Types.Buildable

import bwapi.UnitType

class BuildableUnit(unitType: UnitType) extends Buildable(unit = Some(unitType)){}
