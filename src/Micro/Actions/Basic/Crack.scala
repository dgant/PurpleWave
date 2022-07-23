package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Time.Minutes

object Crack extends Action {

  private def egg(unit: FriendlyUnitInfo): Option[UnitInfo] = {
    unit.zone.units.find(u => u.unitClass.isBuilding && u.isNeutral && unit.canAttack(u))
  }

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canAttack
    && ! unit.unitClass.melee
    && With.frame > Minutes(5)()
    && With.frame < Minutes(8)() // For performance, mainly
    && ! unit.team.exists(_.engagedUpon)
    && unit.base.exists(_.isOurs)
    && unit.matchups.framesOfSafety > unit.cooldownMaxGround
    && unit.pixelDistanceCenter(unit.agent.destination) < 32
    && unit.cooldownLeft == 0
    && egg(unit).isDefined)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toAttack = egg(unit)
    Commander.attack(unit)
  }
}
