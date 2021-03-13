package Micro.Actions.Combat.Targeting.Filters

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterSquad extends TargetFilter {
  simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.squad.exists(_.targetsEnemies)
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    (actor.inRangeToAttack(target) && actor.readyForAttackOrder && target.matchups.targets.nonEmpty)
    || actor.matchups.anchor.isDefined // Let the anchor filter take care of it
    || With.yolo.active()
    || actor.squad.forall(target.squads.contains)
    || target.pixelDistanceTravelling(actor.agent.destination) < actor.pixelDistanceTravelling(actor.agent.destination)
  )
}
