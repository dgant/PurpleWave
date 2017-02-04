package Plans.GamePlans

import Plans.Generic.Compound.AllSimultaneous
import Plans.GamePlans.Protoss.ProtossStrategyCheese

class PlanWinTheGame extends AllSimultaneous {
  setChildren(List(new ProtossStrategyCheese))
}
