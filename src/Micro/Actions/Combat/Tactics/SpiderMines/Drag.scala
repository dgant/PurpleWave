package Micro.Actions.Combat.Tactics.SpiderMines

import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Filters.TargetFilterWhitelist
import Micro.Actions.Combat.Targeting.TargetAction
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class Drag(targets: Iterable[UnitInfo]) extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = true

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val whitelist = TargetFilterWhitelist(targets)
    new TargetAction(whitelist).delegate(unit)
    unit.agent.toAttack.foreach(target => {
      if (unit.pixelDistanceEdge(target) < 32) {
        unit.agent.toTravel = Some(target.pixelCenter)
        Move.delegate(unit)
      }
    })
    Attack.delegate(unit)
  }
}
