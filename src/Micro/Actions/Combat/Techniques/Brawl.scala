package Micro.Actions.Combat.Techniques

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Mathematics.PurpleMath
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.AttackMove
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Brawl extends ActionTechnique {
  
  // In close-quarters fights against other melee units,
  // prefer non-targeted commands to avoid glitching.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.matchups.targets.exists(_.unitClass.melee)
    && unit.matchups.threats.exists(_.unitClass.melee)
  )
  
  override val activator = RMS
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if (other.flying) return None
    if ( ! other.canAttack(unit)) return None
    if ( ! unit.canAttack(other)) return None
    if (other.unitClass.ranged) return Some(0.0)
    
    val output = PurpleMath.nanToOne(GameTime(0, 1)() / other.framesToGetInRange(unit))
    Some(output)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.unitClass.melee)
    Target.delegate(unit)
    unit.agent.toTravel = unit.agent.toAttack.map(_.pixelCenter)
    AttackMove.delegate(unit)
  }
}
