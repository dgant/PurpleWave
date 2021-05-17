package Micro.Targeting.Filters
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterArchonOptional extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = Protoss.Archon(actor)
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    // Archons do huge DPS but have atrocious movement characteristics.
    // They're big dumb melee dragoons with deceleration.
    // So make sure they attack ANY combat units they can get their hands on
    target.unitClass.attacksOrCastsOrDetectsOrTransports && actor.inRangeToAttack(target)
  }
}
