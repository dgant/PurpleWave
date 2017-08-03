package Information.StrategyDetection.Generic

import ProxyBwapi.UnitClass.UnitClass

case class FingerprintCompleteBy(
  unitClass : UnitClass,
  gameTime  : GameTime,
  quantity  : Int = 1)
    extends AbstractFingerprintQuantityBy(unitClass, gameTime) {
  
  override def matches: Boolean = observed >= quantity
}
