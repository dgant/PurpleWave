package Plans.GamePlans

import Plans.Generic.Compound.Simultaneous
import Plans.GamePlans.Protoss.ProtossStrategyCheese

class PlanWinTheGame extends Simultaneous {
  setChildren(List(new ProtossStrategyCheese))
}
