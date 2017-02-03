package Plans.GamePlans

import Plans.Generic.Compound.PlanCompleteAllInParallel
import Plans.GamePlans.Protoss.ProtossStrategyCheese

class PlanWinTheGame extends PlanCompleteAllInParallel {
  kids = List(new ProtossStrategyCheese)
}
