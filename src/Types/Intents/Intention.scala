package Types.Intents

import Global.Combat.Battle.Battle
import Global.Combat.Commands.Command
import Types.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Position

class Intention(
  val unit:FriendlyUnitInfo,
  val command:Command,
  var destination:Option[Position] = None,
  var battle:Option[Battle] = None,
  var targetUnit:Option[UnitInfo] = None) {
}
