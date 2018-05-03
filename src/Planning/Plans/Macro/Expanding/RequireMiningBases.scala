package Planning.Plans.Macro.Expanding

import Debugging.English
import Planning.Plans.Predicates.Milestones.AllMiningBases

class RequireMiningBases(basesInitial: Int = 1) extends RequireBases(basesInitial) {
  
  override def toString: String = "Require " + basesDesired.get + English.pluralize(" base", basesDesired.get)
  
  override protected def basesNow: Int = {
    AllMiningBases().size
  }
}

