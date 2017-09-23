package Planning.Plans.Macro.Milestones

import Planning.Plan
import Planning.Plans.Compound.{If, NoPlan}

class OnGasBases(
  requiredBases : Int,
  argWhenTrue   : Plan = NoPlan(),
  argWhenFalse  : Plan = NoPlan())
  extends If(
    new HaveGasBases(requiredBases),
    argWhenTrue,
    argWhenFalse)
