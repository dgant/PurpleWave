package Micro.Actions.Combat.Targeting.Filters
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterTankFodder extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if (actor.isReaver() && actor.loaded && target.isSiegeTankSieged() && target.cooldownLeft <= actor.cooldownLeft + actor.pixelsToGetInRange(target)) {
      return false
    }
    true
  }
}
