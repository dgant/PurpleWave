package Macro.Scheduling

import ProxyBwapi.UnitClasses.UnitClass

case class MacroProducer(producer: UnitClass, product: UnitClass) {
  val supplyUsePerFrame: Float = product.supplyRequired.toFloat / product.buildFrames
}
