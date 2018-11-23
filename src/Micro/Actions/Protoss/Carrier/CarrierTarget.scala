package Micro.Actions.Protoss.Carrier

import Micro.Actions.Combat.Targeting.TargetAction

object CarrierTarget extends TargetAction(CarrierTargetFilterIgnoreInterceptors) {
  override val additionalFiltersOptional = Vector(
    CarrierTargetFilterInRange,
    CarrierTargetFilterInLeash,
    CarrierTargetFilterShootsUp
  )
}
