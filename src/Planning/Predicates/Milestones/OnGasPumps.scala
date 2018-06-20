package Planning.Predicates.Milestones

import Planning.Plan
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.If

class OnGasPumps(
    requiredPumps : Int,
    argWhenTrue   : Plan = NoPlan(),
    argWhenFalse  : Plan = NoPlan())
  extends If(
    new HaveGasPumps(requiredPumps),
    argWhenTrue,
    argWhenFalse)
