package Planning.Plans.Macro.Expanding

import Planning.Plans.Compound.If
import Planning.Predicates.Economy.GasAtMost

class BuildGasPumpsIfBelow(ceiling: Int) extends If(new GasAtMost(ceiling), new BuildGasPumps)
