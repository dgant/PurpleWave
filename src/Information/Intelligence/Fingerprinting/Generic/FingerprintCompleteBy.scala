package Information.Intelligence.Fingerprinting.Generic

import ProxyBwapi.UnitClass.UnitClass

class FingerprintCompleteBy(
  unitClass : UnitClass,
  gameTime  : GameTime,
  quantity  : Int = 1)
    extends AbstractFingerprintQuantityBy(unitClass, gameTime) {
  
  override def matches: Boolean = observed >= quantity
}
