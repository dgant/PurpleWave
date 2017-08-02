package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Potshot
import Micro.Actions.Commands.Travel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Scout extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toTravel.isDefined  &&
    unit.agent.canScout            &&
    unit.canAttack
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    if ( ! unit.wounded) {
      Potshot.consider(unit)
    }
    PreserveScout.consider(unit)
    DisruptBuilder.consider(unit)
    BlockConstruction.consider(unit)
    FindBuildings.consider(unit)
    Poke.consider(unit)
    Travel.consider(unit)
  }
}
