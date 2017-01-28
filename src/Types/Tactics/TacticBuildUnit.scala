package Types.Tactics

import bwapi.{TilePosition, UnitType}

class TacticBuildUnit(
  unit:bwapi.Unit,
  unitType:UnitType,
  val position:Option[TilePosition]) extends Tactic(unit) {
  
}
