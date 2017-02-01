package Types.Tactics

import Startup.With
import Types.PositionFinders.PositionFinder
import bwapi.{TilePosition, UnitCommandType, UnitType}

class TacticBuildBuildingWithWorker(
  worker:bwapi.Unit,
  buildingType:UnitType,
  positionFinder:PositionFinder)
    extends Tactic(worker) {

  var _position:Option[TilePosition] = None
  var _building:Option[bwapi.Unit] = None
  var _lastReposition = Integer.MIN_VALUE
  
  override def isComplete(): Boolean = {
    _building.exists(_.isCompleted)
  }
  
  override def execute() {
    if (_noBuilding()) {
      if (worker.getLastCommand.getUnitCommandType == UnitCommandType.Build) {
        _position = Some(worker.getLastCommand.getTargetTilePosition)
      }
      _building = With.ourUnits
          .filter(_.getType == buildingType)
          .filter(_.getBuildUnit == worker)
          .headOption
    }
    if (_noBuilding()) {
      if (_lastReposition < With.game.getFrameCount - 24) {
        _position = positionFinder.find()
        _position.foreach(worker.build(buildingType, _))
        _lastReposition = With.game.getFrameCount
      }
    }
    //If Terran building exists but the worker has been replaced
    else if(_building.exists(b => b.getBuildType.getRace == bwapi.Race.Terran && b.getBuildUnit == null)) {
      worker.rightClick(_building.get)
    }
  }
  
  def _noBuilding():Boolean = {
    _building.filter(_.exists).isEmpty
  }
}

