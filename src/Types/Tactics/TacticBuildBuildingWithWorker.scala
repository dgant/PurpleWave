package Types.Tactics

import Startup.With
import Types.PositionFinders.PositionFinder
import bwapi.{UnitCommandType, UnitType}

class TacticBuildBuildingWithWorker(
  worker:bwapi.Unit,
  buildingType:UnitType,
  positionFinder:PositionFinder)
    extends Tactic(worker) {

  var _startedBuilding = false
  var _timeout = Integer.MAX_VALUE
  
  override def isComplete(): Boolean = {
    _startedBuilding && With.game.getFrameCount >= _timeout
  }
  
  override def execute() {

    if (_isBuilding) {
      if ( ! _startedBuilding) {
        _startedBuilding = true
        _resetTimeout()
      }
    }
    else {
      _startedBuilding = false
      _resetTimeout()
      worker.build(buildingType, positionFinder.find())
    }
  }
  
  def _resetTimeout() {
    _timeout = With.game.getFrameCount + buildingType.buildTime
  }
  
  def _isBuilding(): Boolean = {
    worker.getLastCommand.getUnitCommandType == UnitCommandType.Build
  }
}

