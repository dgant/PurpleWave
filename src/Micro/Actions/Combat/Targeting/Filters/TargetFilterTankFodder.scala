package Micro.Actions.Combat.Targeting.Filters
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterTankFodder extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.is(Protoss.Reaver)
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if (actor.is(Protoss.Reaver) && actor.loaded && target.is(Terran.SiegeTankSieged) && target.cooldownLeft <= actor.cooldownLeft + actor.pixelsToGetInRange(target)) {
      return false
    }
    true
  }
}
