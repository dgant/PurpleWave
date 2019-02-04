package Micro.Actions.Combat.Techniques

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Combat.Techniques.Common.Activators.One
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Ignore extends ActionTechnique {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
  )
  
  override val activator = One
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = (
    (if (With.grids.enemyVision.isSet(unit.tileIncludingCenter)) 1.0 else 2.0)
    * unit.matchups.framesOfSafety
    / GameTime(0, 48)()
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {

  }
}
