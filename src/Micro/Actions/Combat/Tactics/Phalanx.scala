package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Disengage, Engage}
import Micro.Actions.Commands.Move
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
            && threat.inRangeToAttack(ally, post)
            && With.grids.enemyVision.isSet(post.tileIncluding)))))) {
      Engage.delegate(unit)
    }
    if (unit.matchups.threats.exists(t => ! unit.canAttack(t) || t.pixelRangeAgainst(unit) > unit.pixelRangeAgainst(t))) {
      Disengage.delegate(unit)
    } else if (unit.agent.toForm.forall(_.pixelDistance(unit.pixelCenter) < 2)){
      With.commander.hold(unit)
    } else {
      unit.agent.toTravel = unit.agent.toForm
      Move.delegate(unit)
    }
  }
}
