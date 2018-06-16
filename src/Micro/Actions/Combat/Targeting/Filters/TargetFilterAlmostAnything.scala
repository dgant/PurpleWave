package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterAlmostAnything extends TargetFilter {
  
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    // Don't attack larvae. Just don't do it.
    //
    if (target.is(Zerg.Larva)) return false
  
    actor.agent.toLeash.forall(leash => (
      actor.inRangeToAttack(target)
      || actor.pixelToFireAt(target).pixelDistance(leash.pixelCenter) < leash.pixelRange
    ))
  }
}
