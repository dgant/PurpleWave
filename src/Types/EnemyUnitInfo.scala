package Types

import Startup.With
import bwapi.{Player, Position, TilePosition, UnitType}

class EnemyUnitInfo(
   var getID:Integer,
   var lastSeen:Integer,
   var possiblyStillThere:Boolean,
   var getPlayer:Player,
   var getPosition:Position,
   var getTilePosition:TilePosition,
   var getHitPoints:Integer,
   var getShields:Integer,
   var getType:UnitType,
   var isCompleted:Boolean) {
  
  def unit:Option[bwapi.Unit] = {
    With.unit(getID)
  }
}
