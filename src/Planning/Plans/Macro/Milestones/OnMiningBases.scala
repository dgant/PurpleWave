package Planning.Plans.Macro.Milestones

import Planning.Plan
import Planning.Plans.Compound.If

class OnMiningBases(
  requiredBases : Int,
  argWhenTrue   : Plan = new Plan,
  argWhenFalse  : Plan = new Plan)
  extends If(
    new MiningBasesAtLeast(requiredBases),
    argWhenTrue,
    argWhenFalse)
