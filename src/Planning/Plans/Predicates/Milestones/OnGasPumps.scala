package Planning.Plans.Predicates.Milestones

import Planning.Plan
import Planning.Plans.Compound.{If, NoPlan}

class OnGasPumps(
    requiredPumps : Int,
    argWhenTrue   : Plan = NoPlan(),
    argWhenFalse  : Plan = NoPlan())
  extends If(
    new HaveGasPumps(requiredPumps),
    argWhenTrue,
    argWhenFalse)
