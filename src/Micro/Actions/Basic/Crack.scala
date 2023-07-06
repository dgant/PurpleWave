package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Time.Minutes

object Crack extends Action {

  private def egg(unit: FriendlyUnitInfo): Option[UnitInfo] = {
    unit.zone.units.find(u => ! u.flying && u.isNeutral && unit.canAttack(u))
  }

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canAttack
    && ! unit.unitClass.melee
    && ! unit.isAny(Terran.SiegeTankSieged, Protoss.Reaver, Zerg.InfestedTerran)
    && With.frame > Minutes(5)()
    && With.frame < Minutes(12)() // For performance, mainly
    && ! unit.team.exists(_.engagedUpon)
    && unit.base.exists(_.isOurs)
    && unit.matchups.framesOfSafety > unit.cooldownMaxGround
    && unit.pixelDistanceCenter(unit.agent.destination) < unit.pixelRangeMax
    && unit.readyForAttackOrder
    && egg(unit).isDefined)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toAttack = egg(unit)
    Commander.attack(unit)
  }
}
