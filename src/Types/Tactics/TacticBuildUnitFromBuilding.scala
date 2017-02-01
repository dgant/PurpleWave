package Types.Tactics

import Startup.With
import bwapi.{TilePosition, UnitCommandType, UnitType}

class TacticBuildUnitFromBuilding(
  unit:bwapi.Unit,
  unitType:UnitType,
  val position:Option[TilePosition] = None)
    extends Tactic(unit) {
  
  var _issuedOrder = false
  var _startedBuilding = false
  var _timeout = Integer.MAX_VALUE
  
  override def isComplete(): Boolean = {
    _issuedOrder && _startedBuilding && With.game.getFrameCount >= _timeout
  }
  
  override def execute() {
    
    if (_startedBuilding) {
    }
    else if (_issuedOrder) {
      if (_isBuildingOrTraining) {
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
  
      _timeout = With.game.getFrameCount + unitType.buildTime
      _issuedOrder = true
    }
  }
  
  def _isBuildingOrTraining(): Boolean = {
    unit.getLastCommand.getUnitCommandType == UnitCommandType.Train
  }
}
