package Micro.Actions.Combat.Targeting.Filters
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterLeash extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
     actor.agent.toLeash.forall(leash => (
       actor.inRangeToAttack(target) || actor.pixelToFireAt(target).pixelDistance(actor.agent.origin) < leash
    ))
  }
}
