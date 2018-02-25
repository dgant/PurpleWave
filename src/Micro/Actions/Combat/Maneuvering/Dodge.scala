package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Lifecycle.With
import Mathematics.Physics.ForceMath
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Commands.Gravitate
import Micro.Coordination.Explosions.Explosion
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Dodge extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && ! Yolo.active
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    lazy val reactionFrames =
      With.reaction.agencyMax
      + unit.unitClass.framesToTurn180
      + unit.unitClass.accelerationFrames
    val explosions = With.coordinator.explosions.nearUnit(unit)
      .filter(explosion =>
        explosion.affects(unit)
        && framesOfEntanglement(unit, explosion) > - reactionFrames)
    
    if (explosions.nonEmpty) {
      val forces = explosions.map(explosion =>
        explosion.directionTo(unit).normalize(framesOfEntanglement(unit, explosion) + reactionFrames))
      
      val force = ForceMath.sum(forces).normalize
      unit.agent.forces.put(ForceColors.threat, force)
      Gravitate.delegate(unit)
    }
  }
  
  protected def framesOfEntanglement(victim: FriendlyUnitInfo, explosion: Explosion): Double = {
    val pixelsOfEntanglement = explosion.pixelsOfEntanglement(victim)
    victim.framesToTravelPixels(Math.abs(pixelsOfEntanglement)) * PurpleMath.signum(pixelsOfEntanglement)
  }
}
