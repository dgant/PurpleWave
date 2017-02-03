package Plans.Generic.Compound

import Plans.Plan

class PlanFulfillRequirements() extends Plan {
  
  var requirement:Plan = null
  var fulfiller:Plan = null
  
  override def children(): Iterable[Plan] = {
    List(requirement, fulfiller)
  }
  
  override def isComplete(): Boolean = {
    requirement.isComplete()
  }

  override def execute() {
    requirement.execute()
    if (requirement.isComplete) {
      fulfiller.abort()
    }
    else {
      fulfiller.execute()
    }
  }
}
