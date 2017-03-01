package Types.Intents

import Global.Combat.Battle.Battle
import Types.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Position

class Intention(
  val unit:FriendlyUnitInfo,
  var destination:Option[Position] = None,
  var targetUnit:Option[UnitInfo] = None) {
  
  var battle:Battle = null
}
