package Macro.Scheduling

import ProxyBwapi.UnitClasses.UnitClass
import Utilities.?

case class MacroProducer(producer: UnitClass, product: UnitClass) {
  val supplyUsePerFrame: Float = product.supplyRequired.toFloat / ?(producer.isHatchlike, 342, product.buildFrames)
}
