package Micro.Actions.Combat.Tactics

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Disengage, Engage}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Phalanx extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    ! unit.flying
    && unit.agent.toForm.exists(p => unit.framesToTravelTo(p) < GameTime(0, 10)()))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val spot      = unit.agent.toForm.get
    val hoplites  = unit.matchups.allies.flatMap(_.friendly).filter(_.agent.toForm.isDefined)
    val besieged  = hoplites.exists(h => h.matchups.threats.exists(t => t.inRangeToAttack(h, h.agent.toForm.get) && h.pixelRangeAgainst(t) < t.pixelRangeAgainst(h)))
    val openFire  = unit.matchups.targets.exists(t => t.pixelDistanceEdge(unit, unit.agent.toForm.get) <= unit.pixelRangeAgainst(t))

    unit.agent.toTravel = Some(spot)
    unit.agent.toReturn = Some(spot)
    if (besieged && unit.agent.shouldEngage) {
      Engage.delegate(unit)
    } else if (openFire) {
      Engage.delegate(unit)
    }
    Disengage.delegate(unit)
  }
}
