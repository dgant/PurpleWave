package Plans.GamePlans

import Plans.GamePlans.Protoss.ProtossStrategyCheese
import Plans.Generic.Compound.AllParallel

class PlanWinTheGame extends AllParallel {
  children.set(List(new ProtossStrategyCheese))
}
