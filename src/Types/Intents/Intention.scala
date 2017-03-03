package Types.Intents

import Global.Combat.Battle.Battle
import Global.Combat.Commands.Command
import Startup.With
import Types.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.TilePosition

class Intention(
  val unit:FriendlyUnitInfo,
  val command:Command,
  var destination:TilePosition) {
  
  var motivation = 1.0
  var safety:TilePosition = With.geography.home
  var battle:Option[Battle] = None
  var targetUnit:Option[UnitInfo] = None
}
