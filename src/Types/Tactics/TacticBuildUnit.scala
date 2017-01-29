package Types.Tactics

import bwapi.{TilePosition, UnitCommandType, UnitType}

class TacticBuildUnit(
  unit:bwapi.Unit,
  unitType:UnitType,
  val position:Option[TilePosition])
    extends Tactic(unit) {
  
  var _issuedOrder = false
  var _startedBuilding = false
  
  override def execute() {
    
    if (_startedBuilding) {
      if ( ! _isBuildingOrTraining()) {
        //TODO: Release!
      }
    }
    else if (_issuedOrder) {
      if (_isBuildingOrTraining()) {
        _startedBuilding = true
      }
    }
    else {
      if (unitType.isBuilding) {
        unit.build(unitType, position.get)
      }
      else {
        unit.train(unitType)
      }
  
      _issuedOrder = true
    }
  }
  
  def _isBuildingOrTraining(): Boolean = {
    var result = false
    result = result || unit.getLastCommand.getUnitCommandType == UnitCommandType.Build
    result = result || unit.getLastCommand.getUnitCommandType == UnitCommandType.Build_Addon
    result = result || unit.getLastCommand.getUnitCommandType == UnitCommandType.Train
    result
  }
}
