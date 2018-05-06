package Information.Intelligenze.Fingerprinting.Generic

import ProxyBwapi.UnitClasses.UnitClass

class FingerprintIncompleteBy(
  unitClass : UnitClass,
  gameTime  : GameTime,
  quantity  : Int = 1)
    extends AbstractFingerprintQuantityBy(unitClass, gameTime) {
  
  override def investigate: Boolean = observed < quantity
}
