package Types.Tactics

import Startup.With
import bwapi.{UnitCommandType, UnitType}

class TacticBuildUnitFromBuilding(
  unit:bwapi.Unit,
  unitType:UnitType)
    extends Tactic(unit) {
  
  var _issuedOrder = false
  var _startedBuilding = false
  var _timeout = Integer.MAX_VALUE
  
  override def isComplete(): Boolean = {
    With.game.getFrameCount >= _timeout
  }
  
  override def execute() {
    
    if (_issuedOrder && _isTraining) {
      if ( ! _startedBuilding) {
        _setTimeout()
      }
      _startedBuilding = true
    }
    else {
      _issuedOrder = false
  
      if (unit.getTrainingQueue.isEmpty) {
        unit.train(unitType)
        _issuedOrder = true
      }
    }
  }
  
  def _setTimeout() {
    _timeout = With.game.getFrameCount + unitType.buildTime
  }
  
  def _isTraining(): Boolean = {
    unit.getLastCommand.getUnitCommandType == UnitCommandType.Train
  }
}
