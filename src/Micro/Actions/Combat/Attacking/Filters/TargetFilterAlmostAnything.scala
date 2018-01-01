package Micro.Actions.Combat.Attacking.Filters

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterAlmostAnything extends TargetFilter {
  
  // Don't attack larvae. Just don't do it.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    ! target.is(Zerg.Larva)
  }
}
