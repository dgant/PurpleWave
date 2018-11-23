package Micro.Actions.Protoss.Carrier

import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object CarrierHoldLeash extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Carrier)
  override def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toAttack.foreach(target => {
      unit.agent.toTravel = Some(target.pixelCenter.project(unit.pixelCenter, 7.5 * 32.0))
      Move.delegate(unit)
    })
  }
}
