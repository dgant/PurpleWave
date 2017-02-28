package Plans.GamePlans

import Plans.Compound.AllParallel
import Plans.GamePlans.Protoss.ProtossStrategyMacro

class WinTheGame extends AllParallel {
  children.set(List(new ProtossStrategyMacro))
}
