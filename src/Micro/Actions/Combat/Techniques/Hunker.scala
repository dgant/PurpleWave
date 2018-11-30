package Micro.Actions.Combat.Techniques

import Lifecycle.With
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.Move
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Hunker extends ActionTechnique {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.toForm.isDefined && unit.matchups.targets.forall(!_.is(UnitMatchSiegeTank))

  override val applicabilityBase: Double = 0.5

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Potshot.delegate(unit)
    if (unit.agent.toForm.exists(_.pixelDistance(unit.pixelCenter) > 4)) {
      unit.agent.toTravel = unit.agent.toForm
      Move.delegate(unit)
    }
    With.commander.hold(unit)
  }
}
