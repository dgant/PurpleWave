package Micro.Actions.Combat.Techniques

import Debugging.Visualizations.ForceColors
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Avoid extends ActionTechnique {
  
  // If our path home is blocked by enemies,
  // try to find an alternate escape route.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.matchups.threats.nonEmpty
  )
  
  override val applicabilityBase: Double = 0.5
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    val meleeFactor   = if (unit.unitClass.ranged) 1.0 else 0.7
    val visionFactor  = if (unit.visibleToOpponents) 1.0 else 0.5
    val safetyFactor  = PurpleMath.clampToOne((36.0 + unit.matchups.framesOfEntanglement) / 24.0)
    val output        = meleeFactor * visionFactor * safetyFactor
    output
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! other.canAttack(unit)) return None
    
    lazy val path = unit.agent.zonePath(unit.agent.origin)
    if ( ! unit.flying && path.isEmpty) return None

    Some(1.0)
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    unit.agent.toTravel = Some(unit.agent.origin)

    val bonusAvoidThreats = PurpleMath.clamp(With.reaction.agencyAverage + unit.matchups.framesOfEntanglement, 12.0, 24.0) / 12.0
    val bonusPreferExit   = if (unit.agent.origin.zone != unit.zone) 1.0 else if (unit.matchups.threats.exists(_.topSpeed < unit.topSpeed)) 0.0 else 0.5
    val bonusRegrouping   = 9.0 / Math.max(24.0, unit.matchups.framesOfEntanglement)
    val bonusMobility     = 1.0
    
    val forceThreat       = Potential.avoidThreats(unit)      * bonusAvoidThreats
    val forceSpacing      = Potential.avoidCollision(unit)
    val forceExiting      = Potential.preferTravelling(unit)  * bonusPreferExit
    val forceSpreading    = Potential.preferSpreading(unit)
    val forceRegrouping   = Potential.preferRegrouping(unit)  * bonusRegrouping
    val forceMobility     = Potential.preferMobility(unit)    * bonusMobility
    val resistancesTerran = Potential.resistTerrain(unit)
    
    unit.agent.forces.put(ForceColors.threat,         forceThreat)
    unit.agent.forces.put(ForceColors.traveling,      forceExiting)
    unit.agent.forces.put(ForceColors.spreading,      forceSpreading)
    unit.agent.forces.put(ForceColors.regrouping,     forceRegrouping)
    unit.agent.forces.put(ForceColors.spacing,        forceSpacing)
    unit.agent.forces.put(ForceColors.mobility,       forceMobility)
    unit.agent.resistances.put(ForceColors.mobility,  resistancesTerran)
    Gravitate.delegate(unit)
    Move.delegate(unit)
  }
}
