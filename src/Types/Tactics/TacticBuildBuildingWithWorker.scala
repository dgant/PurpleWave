package Types.Tactics

import Startup.With
import Types.PositionFinders.PositionFinder
import bwapi.{TilePosition, UnitType}

import scala.collection.JavaConverters._


class TacticBuildBuildingWithWorker(
  worker:bwapi.Unit,
  buildingType:UnitType,
  positionFinder:PositionFinder)
    extends Tactic(worker) {

  var _position:Option[TilePosition] = None
  var _building:Option[bwapi.Unit] = None
  
  override def isComplete(): Boolean = {
    _building.exists(_.isCompleted)
  }
  
  override def execute() {
    if (_building.filter(_.exists).isEmpty) {
      _position = positionFinder.find()
      _position.foreach(worker.build(buildingType, _))
      _building = With.game.self.getUnits.asScala.filter(_.getTilePosition == _position).headOption
    }
  }
}

