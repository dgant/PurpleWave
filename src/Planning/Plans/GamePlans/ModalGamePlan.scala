package Planning.Plans.GamePlans

import Planning.Plan
import Planning.Plans.Compound.Serial

class ModalGamePlan(modes: Plan*) extends Serial(modes: _*) {
  
  description.set("Modal game plan")
  
}