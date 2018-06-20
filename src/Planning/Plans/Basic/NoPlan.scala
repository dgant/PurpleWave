package Planning.Plans.Basic

import Planning.Plan

object NoPlan {
  def apply(): Plan = new Plan { description.set("[Empty]") }
}
