package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}

object TargetFilterAntiAir extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.isAny(Terran.Battlecruiser, Protoss.Carrier, Zerg.Mutalisk) || actor.matchups.anchor.exists(_.isAny(Terran.Battlecruiser, Protoss.Carrier, Zerg.Mutalisk))
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    target.unitClass.attacksAir || target.order == Orders.Repair && target.orderTarget.exists(_.unitClass.attacksAir)
  }
}
