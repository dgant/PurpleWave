package Micro.Actions.Combat.Targeting.Filters

import Lifecycle.With
import Micro.Agency.AnchorMargin
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterSquad extends TargetFilter {
  simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.squad.exists(_.targetsEnemies)
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    (actor.inRangeToAttack(target) && actor.readyForAttackOrder && target.matchups.targets.nonEmpty)
    || With.yolo.active()
    || actor.squad.forall(target.squads.contains)
    || (actor.topSpeed > target.topSpeed && actor.pixelDistanceTravelling(actor.agent.destination) >= actor.pixelToFireAt(target).travelPixelsFor(actor.agent.destination, actor))
    || actor.matchups.anchors.exists(a => target.canAttack(a) && target.pixelsToGetInRange(a) < AnchorMargin())
  )
}
