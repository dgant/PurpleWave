package Micro.Actions.Combat.Techniques

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Combat.Maneuvering.{GooseChase, OldAvoid}
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Avoid extends ActionTechnique {
  
  // If our path home is blocked by enemies,
  // try to find an alternate escape route.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.matchups.threats.nonEmpty
  )
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    val meleeFactor   = if (unit.unitClass.ranged) 1.0 else 0.75
    val visionFactor  = if (With.grids.enemyVision.isSet(unit.tileIncludingCenter)) 0.5 else 1.0
    val safetyFactor  = PurpleMath.clampToOne((36.0 + unit.matchups.framesOfEntanglementDiffused) / 24.0)
    val output        = meleeFactor * visionFactor * safetyFactor
    output
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! other.canAttack(unit)) return None
    
    val path = unit.agent.zonePath(unit.agent.origin)
    if (path.isEmpty) return None
    
    val pixelStep       = 16.0
    val baseValue       = 0.25
    val here            = unit.pixelCenter
    val there           = unit.pixelCenter.project(unit.agent.nextWaypoint(unit.agent.origin), pixelStep)
    val distanceHere    = other.pixelDistanceCenter(here)
    val distanceThere   = other.pixelDistanceCenter(there)
    val blockingFactor  = (distanceHere - distanceThere) / pixelStep
    val output          = baseValue + (1.0 - baseValue) * Math.max(blockingFactor, 0)
    Some(output)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    OldAvoid.delegate(unit)
  }
}
