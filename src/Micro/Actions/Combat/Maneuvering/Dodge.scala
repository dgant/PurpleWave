package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Lifecycle.With
import Mathematics.Physics.ForceMath
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Coordination.Explosions.{Explosion, ExplosionSpiderMineTrigger}
import Micro.Decisions.Potential
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Dodge extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && ! Yolo.active
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    lazy val canAttackMines = unit.unitClass.attacksGround && unit.pixelRangeGround > 96.0
    
    lazy val reactionFrames =
      With.reaction.agencyMax
      + unit.unitClass.framesToTurn180
      + unit.unitClass.accelerationFrames
    val explosions = With.coordinator.explosions.nearUnit(unit)
      .filter(explosion =>
        explosion.affects(unit)
        && framesOfEntanglement(unit, explosion) > - reactionFrames)
    
    if (explosions.nonEmpty) {
      if (canAttackMines
        && explosions.forall(_.isInstanceOf[ExplosionSpiderMineTrigger])
        && unit.matchups.allies.exists(_.unitClass.isDetector)) {
        return
      }
      
      val forces = explosions.map(explosion =>
        explosion.directionTo(unit).normalize(framesOfEntanglement(unit, explosion) + reactionFrames))
      
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
  
  protected def framesOfEntanglement(victim: FriendlyUnitInfo, explosion: Explosion): Double = {
    val pixelsOfEntanglement = explosion.pixelsOfEntanglement(victim)
    victim.framesToTravelPixels(Math.abs(pixelsOfEntanglement)) * PurpleMath.signum(pixelsOfEntanglement)
  }
}
