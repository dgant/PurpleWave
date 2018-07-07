package Micro.Actions.Basic

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Disengage, Engage}
import Micro.Actions.Combat.Tactics.Potshot
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object Gather extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toGather.isDefined
  }
  
  private val combatWindow = GameTime(0, 2)()
  
  override def perform(unit: FriendlyUnitInfo) {

    // Performance optimization
    if (unit.battle.isEmpty) {
      With.commander.gather(unit, unit.agent.toGather.get)
      return
    }

    Potshot.consider(unit)
    
    lazy val resource     = unit.agent.toGather.get
    lazy val zoneNow      = unit.zone
    lazy val zoneTo       = resource.zone
    lazy val mainAndNat   = Vector(With.geography.ourMain, With.geography.ourNatural).map(_.zone)
    lazy val transferring = ! unit.base.exists(_.owner.isUs) && zoneNow != zoneTo && ! (mainAndNat.contains(zoneNow) && mainAndNat.contains(zoneTo))
    lazy val threatened   = unit.battle.isDefined && unit.matchups.framesOfSafety < combatWindow && unit.matchups.threats.exists( ! _.unitClass.isWorker)
    lazy val damageMax    = ByOption.max(unit.matchups.threats.map(_.damageOnNextHitAgainst(unit))).getOrElse(11)
    lazy val threatCloser = unit.matchups.threats.exists(_.pixelDistanceCenter(resource.pixelCenter) < unit.pixelDistanceCenter(resource.pixelCenter))
    lazy val atResource   = unit.pixelDistanceCenter(resource) < With.configuration.workerDefenseRadiusPixels
    lazy val beckoned     = unit.battle.isDefined && unit.matchups.targets.exists(target =>
      ! target.unitClass.isWorker
      && target.pixelDistanceCenter(unit) < With.configuration.workerDefenseRadiusPixels
      && target.base.exists(_.units.exists(resource => resource.resourcesLeft > 0 && target.pixelDistanceCenter(resource) < With.configuration.workerDefenseRadiusPixels)))
    
    if (atResource && unit.totalHealth > damageMax && beckoned) {
      Engage.consider(unit)
    }
    
    if (transferring && threatened && unit.visibleToOpponents && threatCloser) {
      unit.agent.canFight = false
      Disengage.consider(unit)
    }
    
    With.commander.gather(unit, unit.agent.toGather.get)
  }
}
