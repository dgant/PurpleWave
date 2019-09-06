package Micro.Actions.Combat.Techniques

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Combat.Techniques.Common.Activators.One
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Ignore extends ActionTechnique {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && (unit.flying || ! unit.zone.edges.exists(_.contains(unit.pixelCenter)))
    && (unit.flying || With.coordinator.gridPathOccupancy.get(unit.tileIncludingCenter) <= 0)
  )
  
  override val activator = One
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = (
    PurpleMath.clamp(unit.matchups.maxTilesOfInvisibility / 3, 1, 4)
    * (if (unit.squad.exists(_.enemies.forall( ! unit.matchups.threats.contains(_)))) 2.0 else 1.0)
    * unit.matchups.framesOfSafety
    / GameTime(0, 12)()
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {}
}
