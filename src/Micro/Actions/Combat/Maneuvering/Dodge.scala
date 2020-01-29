package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Lifecycle.With
import Mathematics.Physics.ForceMath
import Micro.Actions.Action
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Coordination.Explosions.Explosion
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class Dodge(explosions: Iterable[Explosion]) extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    DodgeAll.allowed(unit) && explosions.nonEmpty
  )

  override protected def perform(unit: FriendlyUnitInfo): Unit = {

    if (unit.canBurrow && explosions.forall(_.canBurrowAway(unit))) {
      With.commander.burrow(unit)
    }

    val forces = explosions.map(explosion =>
      explosion
        .directionTo(unit)
        .normalize(
          DodgeAll.framesOfEntanglement(unit, explosion)
          + DodgeAll.reactionFrames(unit)))

    val forceDodging = ForceMath.sum(forces).normalize
    val forcesSpacing = Potential.resistTerrain(unit)
    val forcesMobility = Potential.collisionResistances(unit)
    unit.agent.forces.put(ForceColors.threat, forceDodging)
    unit.agent.resistances.put(ForceColors.spacing, forcesSpacing)
    unit.agent.resistances.put(ForceColors.mobility, forcesMobility)
    Gravitate.delegate(unit)
    Move.delegate(unit)
  }
}
