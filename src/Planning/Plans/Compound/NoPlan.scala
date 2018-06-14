package Planning.Plans.Compound

import Planning.{Plan, Predicate}

object NoPlan {
  def apply(): Plan = new Predicate { description.set("[Empty]") }
}
