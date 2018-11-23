package Micro.Actions.Protoss.Carrier

import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object CarrierChase extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Carrier)
  override def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toAttack.foreach(t => {
      unit.agent.toTravel = Some(unit.pixelCenter.project(t.pixelCenter, 32.0 * 8.0))
      Move.delegate(unit)
    })
  }
}