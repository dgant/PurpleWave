package Planning.Plans.Macro.Expansion

import Lifecycle.With
import Planning.Plans.Compound.{If, IfThenElse}

class WhenMinedOutExpand
  extends IfThenElse(
    new If(() => With.geography.ourBases.map(_.mineralsLeft).sum < With.configuration.maxMineralsBeforeMinedOut),
    new Expand)