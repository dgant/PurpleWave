package Types

import bwapi.{Player, Position, TilePosition, UnitType}

class EnemyUnitInfo(
                     var getID:Integer,
                     var lastSeen:Integer,
                     var getPlayer:Player,
                     var getPosition:Position,
                     var getTilePosition:TilePosition,
                     var getHitPoints:Integer,
                     var getShields:Integer,
                     var getType:UnitType,
                     var isCompleted:Boolean) {
}
