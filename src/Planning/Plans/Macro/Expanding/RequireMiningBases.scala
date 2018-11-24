package Planning.Plans.Macro.Expanding

import Debugging.English
import Planning.Predicates.Milestones.AllMiningBases

class RequireMiningBases(basesInitial: Int = 1) extends RequireBases(basesInitial) {
  
  description.set("Require " + basesDesired.get + English.pluralize(" mining base", basesDesired.get))
  
  override protected def basesNow: Int = {
    AllMiningBases().size
  }
}
