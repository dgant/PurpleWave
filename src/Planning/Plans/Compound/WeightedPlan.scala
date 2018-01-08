package Planning.Plans.Compound

import Planning.Plan

abstract class WeightedPlan extends Plan {
  
  def allowed: Boolean = true
  def weight: Double
  
}
