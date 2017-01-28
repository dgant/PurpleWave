package Types.Tactics

import bwapi.{TilePosition, UnitCommandType, UnitType}

class TacticBuildUnit(
  unit:bwapi.Unit,
  unitType:UnitType,
  val position:Option[TilePosition])
    extends Tactic(unit) {
  
  override def execute() {
    var canDo = true
    canDo = canDo && unit.getLastCommand.getUnitCommandType != UnitCommandType.Build
    canDo = canDo && unit.getLastCommand.getUnitCommandType != UnitCommandType.Build_Addon
    canDo = canDo && unit.getLastCommand.getUnitCommandType != UnitCommandType.Train
    
    if (canDo) {
      if (unitType.isBuilding) {
        unit.build(unitType, position.get)
      }
      else {
        unit.train(unitType)
      }
    }
  }
}
