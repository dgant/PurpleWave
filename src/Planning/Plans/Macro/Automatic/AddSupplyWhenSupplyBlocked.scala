package Planning.Plans.Macro.Automatic

import Planning.Plans.Compound.If
import Planning.Plans.Predicates.Economy.SupplyBlocked

class AddSupplyWhenSupplyBlocked extends If(
  new SupplyBlocked,
  new RequireSufficientSupply)