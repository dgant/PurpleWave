package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Disengage, Engage}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Phalanx extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.toForm.isDefined && ! unit.flying

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toTravel = unit.agent.toForm
    unit.agent.toReturn = unit.agent.toForm
    Potshot.delegate(unit)

    // If enemy is in range to attack one of our allies in formation, fight
    // Otherwise, retreat to formation point
    if (unit.matchups.targets.nonEmpty
      && unit.agent.canFight
      && unit.matchups.threats.exists(threat =>
        threat.matchups.targetsInRange.exists(_.unitClass.isBuilding)
        || threat.matchups.targets.exists(ally =>
          ally.friendly.exists(_.agent.toForm.exists(post =>
            threat.inRangeToAttack(ally)
            && threat.inRangeToAttack(ally, post)))))) {
      Engage.delegate(unit)
    }
    Disengage.delegate(unit)
  }
}
