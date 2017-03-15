package Micro.Intentions

import Micro.Battles.Battle
import Micro.Behaviors.Behavior
import Planning.Plan
import Startup.With
import BWMirrorProxy.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.TilePosition

class Intention(
   val plan:Plan,
   val unit:FriendlyUnitInfo,
   val command:Behavior,
   var destination:TilePosition) {
  
  var motivation = 1.0
  var safety:TilePosition = With.geography.home
  var battle:Option[Battle] = None
  var targetUnit:Option[UnitInfo] = None
}
