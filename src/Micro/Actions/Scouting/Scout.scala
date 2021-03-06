package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Scout extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.toTravel.isDefined && unit.agent.isScout
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Sabotage.consider(unit)
    KnockKnock.consider(unit)
    PreserveScout.consider(unit)
    DisruptBuilder.consider(unit)
    BlockConstruction.consider(unit)
    Search.consider(unit)
    //Poke.consider(unit)
    SearchWhenBored.consider(unit)
    if (unit.matchups.threatsInRange.isEmpty) {
      Potshot.consider(unit)
    }
    Commander.move(unit)
  }
}
