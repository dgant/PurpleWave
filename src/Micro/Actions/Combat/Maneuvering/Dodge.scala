package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Lifecycle.With
import Mathematics.Physics.ForceMath
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Coordination.Explosions.Explosion
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class Dodge(explosions: Iterable[Explosion]) extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    DodgeAll.allowed(unit)
    && explosions.nonEmpty
  )

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val relevantExplosions = explosions.filter(explosion =>
      explosion.affects(unit)
      && isClose(unit, explosion))

    val forces = relevantExplosions.map(explosion =>
      explosion
        .directionTo(unit)
        .normalize(
          framesOfEntanglement(unit, explosion)
          + reactionFrames(unit)))

    val forceDodging = ForceMath.sum(forces).normalize
    val forcesSpacing = Potential.resistTerrain(unit)
    val forcesMobility = Potential.collisionResistances(unit)
    unit.agent.forces.put(ForceColors.threat, forceDodging)
    unit.agent.resistances.put(ForceColors.spacing, forcesSpacing)
    unit.agent.resistances.put(ForceColors.mobility, forcesMobility)
    Gravitate.delegate(unit)
    Move.delegate(unit)
  }

  protected def isClose(unit: FriendlyUnitInfo, explosion: Explosion): Boolean = {
    framesOfEntanglement(unit, explosion) > reactionFrames(unit)
  }

  protected def reactionFrames(unit: FriendlyUnitInfo): Int = (
    With.reaction.agencyMax
      + unit.unitClass.framesToTurn180
      + unit.unitClass.accelerationFrames
    )

  protected def framesOfEntanglement(victim: FriendlyUnitInfo, explosion: Explosion): Double = {
    val pixelsOfEntanglement = explosion.pixelsOfEntanglement(victim)
    victim.framesToTravelPixels(Math.abs(pixelsOfEntanglement)) * PurpleMath.signum(pixelsOfEntanglement)
  }
}
