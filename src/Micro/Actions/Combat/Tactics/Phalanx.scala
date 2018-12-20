package Micro.Actions.Combat.Tactics

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
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
    val openFire  = unit.matchups.targets.exists(t => t.pixelDistanceEdge(unit, unit.agent.toForm.get) <= unit.pixelRangeAgainst(t))
    val besieged = hoplites.exists(hoplite =>
      hoplite.matchups.threats.exists(threat =>
        threat.inRangeToAttack(hoplite, hoplite.agent.toForm.get)
        && hoplite.pixelRangeAgainst(threat) < threat.pixelRangeAgainst(hoplite)
        && With.grids.enemyVision.isSet(hoplite.agent.toForm.get.tileIncluding)))

    unit.agent.toTravel = Some(spot)
    unit.agent.toReturn = Some(spot)
    if (unit.pixelDistanceCenter(unit.agent.toForm.get) <= (if (unit.matchups.framesOfSafety > 24) 8 else 0)
      && unit.unitClass.melee
      && unit.matchups.threats.forall(_.unitClass.melee) ) {
      With.commander.hold(unit)
    } else if (besieged && unit.agent.shouldEngage) {
      Engage.delegate(unit)
    } else if (openFire) {
      Engage.delegate(unit)
    }
    Disengage.delegate(unit)
  }
}
