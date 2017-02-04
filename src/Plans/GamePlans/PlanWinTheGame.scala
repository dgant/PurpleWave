package Plans.GamePlans

import Plans.GamePlans.Protoss.ProtossStrategyCheese
import Plans.Generic.Compound.AllSimultaneous

class PlanWinTheGame extends AllSimultaneous {
  children.set(List(new ProtossStrategyCheese))
}
