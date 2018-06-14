package Planning.Predicates.Milestones

import Planning.Plan
import Planning.Plans.Compound.{If, NoPlan}

class IfOnMiningBases(
  requiredBases : Int,
  argWhenTrue   : Plan = NoPlan(),
  argWhenFalse  : Plan = NoPlan())
  extends If(
    new MiningBasesAtLeast(requiredBases),
    argWhenTrue,
    argWhenFalse)
