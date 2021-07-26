package Micro.Actions.Protoss

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object BeReaver extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Reaver(unit)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    considerHopping(unit)
  }

  def considerHopping(unit: FriendlyUnitInfo) {
    if (unit.agent.commit) return
    if (unit.agent.ride.isEmpty) return
    if (unit.transport.isDefined) return
    lazy val canShoot             = unit.scarabs > 0 && unit.matchups.targetsInRange.nonEmpty
    lazy val inRangeOfPlebian     = unit.matchups.threatsInRange.exists(t => ! t.flying && t.pixelRangeAgainst(unit) < unit.pixelRangeAgainst(t))
    lazy val needlesslyEndangered = unit.cooldownLeft >= unit.cooldownMaxGround / 2 && (inRangeOfPlebian || ! canShoot)
    lazy val needlesslyDoomed     = unit.cooldownLeft >= unit.doomedInFrames
    if (needlesslyEndangered || needlesslyDoomed) {
      unit.agent.act("Hop")
      demandPickup(unit)
      Retreat.consider(unit)
    }
  }

  def demandPickup(unit: FriendlyUnitInfo): Unit = {
    // TODO: This should be arbitrated so shuttle controls itself
    unit.agent.ride.filter(_.agent.passengersPrioritized.find( ! _.loaded).contains(unit)).foreach(Commander.rightClick(unit, _))
  }
}
