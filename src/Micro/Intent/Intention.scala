package Micro.Intent

import Information.Battles.Simulation.Tactics.Tactics
import Lifecycle.With
import Mathematics.Pixels.Tile
import Micro.Behaviors.{MovementProfiles, TargetingProfiles}
import Micro.State.ExecutionState
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade

class Intention(val plan:Plan, val unit:FriendlyUnitInfo) {
  
  def state:ExecutionState = With.executor.getState(unit)
  def tactics:Option[Tactics] = With.battles.byUnit.get(unit).flatMap(b => b.bestSimulationResult.map(s => s.us.tactics))
  
  var executed:Boolean = false
  
  var origin      : Tile              = if (With.geography.ourBases.nonEmpty) With.geography.ourBases.map(_.heart).minBy(unit.travelPixels) else With.geography.home
  var destination : Option[Tile]      = None
  var toAttack    : Option[UnitInfo]  = None
  var toGather    : Option[UnitInfo]  = None
  var toBuild     : Option[UnitClass] = None
  var toTrain     : Option[UnitClass] = None
  var toTech      : Option[Tech]      = None
  var toUpgrade   : Option[Upgrade]   = None
  var leash       : Option[Int]       = None
  var canAttack   : Boolean           = true
  
  lazy val targets = Targets.get(this)
  lazy val threats = Threats.get(this)
 
  var movementProfile = MovementProfiles.default
  var targetProfile   = TargetingProfiles.default
}
