package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Commands.Move
import Micro.Coordination.Pathing.{DesireProfile, MicroPathing}
import Planning.UnitMatchers.UnitMatchWorkers
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Retreat extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && unit.matchups.threats.nonEmpty

  override def perform(unit: FriendlyUnitInfo): Unit = {
    val desire = new DesireProfile(unit)
    retreatZealotsDirectly(unit)
    retreatFliers(unit, desire)
    retreatForceDirect(unit, desire)
    retreatRealPath(unit, desire)
    retreatDirectlyHome(unit, desire)
    retreatForcePotential(unit, desire)
  }

  def retreatZealotsDirectly(unit: FriendlyUnitInfo): Unit = {
    if ( ! unit.ready) return
    // Don't spray Zealots out against melee units, especially Zerglings
    if (unit.is(Protoss.Zealot)
      && unit.base == unit.agent.origin.base
      && unit.agent.origin.base.exists(_.isOurMain)
      && unit.matchups.threats.exists( ! _.unitClass.isWorker)
      && unit.matchups.threats.forall(_.isAny(Protoss.Zealot, Zerg.Zergling, UnitMatchWorkers))) {
      unit.agent.toTravel = unit.agent.origin.base.map(_.heart.pixelCenter)

      // Poke back at enemies -- likely Zerglings -- while retreating
      if ( ! unit.matchups.threats.exists(_.is(Protoss.Zealot))) {
        Potshot.delegate(unit)
      }
      Move.delegate(unit)
      return
    }
  }

  def retreatFliers(unit: FriendlyUnitInfo, desire: DesireProfile): Unit = {
    if ( ! unit.ready) return
    if (unit.flying || (unit.transport.exists(_.flying) && unit.matchups.framesOfSafety <= 0)) {
      retreatForcePotential(unit, desire)
      return
    }
  }

  def retreatForceDirect(unit: FriendlyUnitInfo, desire: DesireProfile): Unit = {
    if ( ! unit.ready) return
    val force = MicroPathing.getAvoidDirectForce(unit, desire)
    force.foreach(MicroPathing.applyDirectForce(unit, desire, _))
  }

  def retreatRealPath(unit: FriendlyUnitInfo, desire: DesireProfile): Unit = {
    if ( ! unit.ready) return
    val path = MicroPathing.getRealPath(unit, desire)
    new Traverse(path).delegate(unit)
  }

  def retreatForcePotential(unit: FriendlyUnitInfo, desire: DesireProfile): Unit = {
    if ( ! unit.ready) return
    MicroPathing.setRetreatPotentials(unit, desire)
    MicroPathing.setDestinationUsingAgentForces(unit)
    Move.delegate(unit)
  }

  def retreatDirectlyHome(unit: FriendlyUnitInfo, desire: DesireProfile): Unit = {
    if ( ! unit.ready) return
    val originBase = unit.agent.origin.base
    if (desire.home >= 0 || (originBase.isDefined && unit.base != originBase && ! unit.matchups.threats.exists(_.base == originBase))) {
      unit.agent.toTravel = Some(unit.agent.origin)
      Move.delegate(unit)
    }
  }
}
