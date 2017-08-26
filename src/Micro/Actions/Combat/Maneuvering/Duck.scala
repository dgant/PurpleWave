package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Micro.Actions.Action
import Micro.Actions.Commands.Gravitate
import Micro.Agency.Explosion
import Micro.Decisions.Potential
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

import scala.collection.mutable.ListBuffer

object Duck extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    unit.agent.explosions ++= getExplosions(unit)
    
    if (unit.agent.explosions.nonEmpty) {
      val forceExplosion  = Potential.explosionsRepulsion(unit).normalize(3.0)
      val forceMobility   = Potential.barrierRepulsion(unit).normalize(2.0)
      val forceSpreading  = Potential.collisionRepulsion(unit)
      unit.agent.forces.put(ForceColors.threat,     forceExplosion)
      unit.agent.forces.put(ForceColors.mobility,   forceMobility)
      unit.agent.forces.put(ForceColors.spreading,  forceSpreading)
      Gravitate.delegate(unit)
    }
  }
  
  private def getExplosions(unit: FriendlyUnitInfo): Iterable[Explosion] = {
    val output = new ListBuffer[Explosion]
    
    output ++= unit.matchups.threats.flatMap(threat => explosion(unit, threat))
    
    output.filter(explosion => explosion.safetyRadius > unit.pixelDistanceFast(explosion.pixelCenter))
  }
  
  private def explosion(unit: FriendlyUnitInfo, threat: UnitInfo): Option[Explosion] = {
    
    if (threat.is(Terran.SpiderMine)
      && ! unit.flying
      && ! unit.unitClass.floats) {
      if (threat.burrowed) {
        
        // Avoid activating the Spider Mine.
        // Spider Mines rely on normal target acquisition range math, which is 96px + attack range edge-to-edge
        // However, this is often limited by Spider Mine sight range, which is 96px exactly.
        // According to jaj22 the acqusition range is center-to-center (unlike most units, which are edge-to-edge)
        val safetyMarginPixels = 96.0
        Some(Explosion(
          threat.pixelCenter,
          32.0 * 3.0 + safetyMarginPixels,
          threat.damageOnNextHitAgainst(unit)
        ))
      }
      else {
        
        // Don't run if we're the target -- instead let other people run away from us.
        lazy val target = threat.target.orElse(ByOption.minBy(threat.matchups.targets)(_.pixelDistanceFast(threat)))
        if (threat.moving && ! target.contains(unit)) {
          val safetyMarginPixels = 0.0
          Some(Explosion(
            threat.projectFrames(4),
            32.0 * 2.0 + 10.0 + unit.unitClass.radialHypotenuse + safetyMarginPixels,
            threat.damageOnNextHitAgainst(unit) // 3 is the range; 1 is the safety margin to avoid triggering it
          ))
        }
        else {
          None
        }
      }
    }
    else if (threat.is(Protoss.Scarab) && ! unit.flying) {
      
      // Don't run if we're the target -- instead let other people run away from us.
      val target = threat.target.orElse(ByOption.minBy(threat.matchups.targets)(_.pixelDistanceFast(threat)))
      if ( ! target.contains(unit)) {
        val targetPixel = target.map(_.pixelCenter).getOrElse(threat.projectFrames(4))
        val safetyMarginPixels = 32.0
        Some(Explosion(
          targetPixel,
          60.0 * + unit.unitClass.radialHypotenuse + safetyMarginPixels,
          threat.damageOnNextHitAgainst(unit)
        ))
      }
      else {
        None
      }
    }
    else {
      None
    }
  }
}
