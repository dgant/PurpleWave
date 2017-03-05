package Plans.GamePlans

import Plans.Compound.AllParallel

class WinTheGame extends AllParallel {
  children.set(List(new ProtossGamePlan))
}
