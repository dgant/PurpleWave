package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plans.Compound.{Check, If}

class AddSupplyWhenSupplyBlocked extends If(
  new Check(() => With.self.supplyUsed >= With.self.supplyTotal),
  new RequireSufficientSupply)