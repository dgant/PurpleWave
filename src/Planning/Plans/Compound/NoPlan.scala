package Planning.Plans.Compound

import Planning.Plan

object NoPlan {
  def apply(): Plan = new Plan { description.set("[Empty]") }
}
