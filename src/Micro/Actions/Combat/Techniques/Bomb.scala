package Micro.Actions.Combat.Techniques

import Lifecycle.With
import Mathematics.Points.PixelRay
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Combat.Techniques.Common.Activators.WeightedMin
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Bomb extends ActionTechnique {
  
  // Let Guardians abuse their range
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Zerg.Guardian)
  
  override val activator = new WeightedMin(this)
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if ( ! other.canAttack(unit)) return None
    if (other.flying) return Some(0.0)
    
    val ray = PixelRay(other.pixelCenter, unit.pixelCenter)
    val traversableTiles = ray
      .tilesIntersected
      .takeWhile(tile => tile.valid && With.grids.walkable.get(tile))
    val firingPixel = traversableTiles.lastOption.map(_.pixelCenter).getOrElse(other.pixelCenter)
    
    if (other.inRangeToAttack(unit, firingPixel)) return Some(0.0)
    
    Some(1.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Breathe.delegate(unit)
  }
}
