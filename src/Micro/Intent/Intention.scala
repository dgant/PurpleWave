package Micro.Intent

import Lifecycle.With
import Micro.Behaviors.{MovementProfiles, TargetingProfiles}
import Micro.State.ExecutionState
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import bwapi.TilePosition

class Intention(val plan:Plan, val unit:FriendlyUnitInfo) {
  
  def state:ExecutionState = With.executor.getState(unit)
  
  var executed:Boolean = false
  
  var destination : Option[TilePosition]  = None
  var toAttack    : Option[UnitInfo]      = None
  var toGather    : Option[UnitInfo]      = None
  var toBuild     : Option[UnitClass]     = None
  var toTrain     : Option[UnitClass]     = None
  var toTech      : Option[Tech]          = None
  var toUpgrade   : Option[Upgrade]       = None
  
  var leash         : Int     = Int.MaxValue
  var desireToFight : Double  = 1.0
  
  lazy val targets = Targets.get(this)
  lazy val threats = Threats.get(this)
 
  var movementProfile = MovementProfiles.default
  var targetProfile   = TargetingProfiles.default
  
  
}
