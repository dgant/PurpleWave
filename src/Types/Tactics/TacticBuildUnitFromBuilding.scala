package Types.Tactics

import Startup.With
import bwapi.{TilePosition, UnitCommandType, UnitType}

class TacticBuildUnitFromBuilding(
  unit:bwapi.Unit,
  unitType:UnitType,
  val position:Option[TilePosition] = None)
    extends Tactic(unit) {
  
  var _startedBuilding = false
  var _timeout = Integer.MAX_VALUE
  
  override def isComplete(): Boolean = {
    With.game.getFrameCount >= _timeout
  }
  
  override def execute() {
    
    if (_isBuildingOrTraining) {
      if ( ! _startedBuilding) {
        _setTimeout()
      }
      _startedBuilding = true
    }
    else if (unit.getTrainingQueue.isEmpty) {
      unit.train(unitType)
    }
  }
  
  def _setTimeout() {
    _timeout = With.game.getFrameCount + unitType.buildTime
  }
  
  def _isBuildingOrTraining(): Boolean = {
    unit.getLastCommand.getUnitCommandType == UnitCommandType.Train
  }
}
