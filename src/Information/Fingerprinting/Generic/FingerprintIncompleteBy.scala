package Information.Fingerprinting.Generic

import Lifecycle.With
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.GameTime

class FingerprintIncompleteBy(
  unitClass : UnitClass,
  gameTime  : GameTime,
  quantity  : Int = 1)
    extends AbstractFingerprintQuantityBy(unitClass, gameTime) {
  
  override def investigate: Boolean = With.frame < gameTime() && observed < quantity
}
