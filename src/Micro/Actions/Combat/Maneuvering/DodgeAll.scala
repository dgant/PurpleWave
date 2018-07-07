package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Coordination.Explosions.Explosion
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object DodgeAll extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && ! Yolo.active
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val explosions = With.coordinator.explosions
      .nearUnit(unit)
      .filter(e => e.affects(unit) && isClose(unit, e))

    if (explosions.nonEmpty) {
      new Dodge(explosions).consider(unit)
    }
  }

  def reactionFrames(unit: FriendlyUnitInfo): Int = (
    With.reaction.agencyMax
      + unit.unitClass.framesToTurn180
      + unit.unitClass.accelerationFrames
  )

  def framesOfEntanglement(victim: FriendlyUnitInfo, explosion: Explosion): Double = {
    val pixelsOfEntanglement = explosion.pixelsOfEntanglement(victim)
    victim.framesToTravelPixels(Math.abs(pixelsOfEntanglement)) * PurpleMath.signum(pixelsOfEntanglement)
  }

  protected def isClose(unit: FriendlyUnitInfo, explosion: Explosion): Boolean = {
    framesOfEntanglement(unit, explosion) > reactionFrames(unit)
  }
}
