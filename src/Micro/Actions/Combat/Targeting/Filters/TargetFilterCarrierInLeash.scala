package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCarrierInLeash extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if ( ! actor.is(Protoss.Carrier)) return true

    (target.pixelDistanceEdge(actor) < 32.0 * 10.0 && actor.interceptors.exists(_.pixelCenter != actor.pixelCenter))
  }

}