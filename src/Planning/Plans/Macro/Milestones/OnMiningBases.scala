package Planning.Plans.Macro.Milestones

import Planning.Plan
import Planning.Plans.Compound.{If, NoPlan}

class OnMiningBases(
  requiredBases : Int,
  argWhenTrue   : Plan = NoPlan,
  argWhenFalse  : Plan = NoPlan)
  extends If(
    new MiningBasesAtLeast(requiredBases),
    argWhenTrue,
    argWhenFalse)
