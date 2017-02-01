package Types.Tactics

package Types.Tactics

import Startup.With
import bwapi.{TilePosition, UnitCommandType, UnitType}

class TacticBuildBuildingWithWorker(
  worker:bwapi.Unit,
  buildingType:UnitType,
  val position:Option[TilePosition])
    extends Tactic(worker) {
  
  var _issuedOrder = false
  var _startedBuilding = false
  var _timeout = Integer.MAX_VALUE
  
  override def isComplete(): Boolean = {
    _issuedOrder && _startedBuilding && With.game.getFrameCount >= _timeout
  }
  
  override def execute() {
  
    worker.build(buildingType, position.get)
    
    /*
    if (_startedBuilding) {
    }
    else if (_issuedOrder) {
      if (_isBuildingOrTraining) {
        _startedBuilding = true
      }
    }
    else {
      if (buildingType.isBuilding) {
        worker.build(buildingType, position.get)
      }
      else {
        worker.train(buildingType)
      }
      
      _timeout = With.game.getFrameCount + buildingType.buildTime
      _issuedOrder = true
    }
    */
  }
  
  def _isBuildingOrTraining(): Boolean = {
    var result = false
    result = result || worker.getLastCommand.getUnitCommandType == UnitCommandType.Build
    result
  }
}

