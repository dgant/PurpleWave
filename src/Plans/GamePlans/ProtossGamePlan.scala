package Plans.GamePlans

import Plans.Information.SwitchEnemyRace

class ProtossGamePlan extends SwitchEnemyRace {
  terran.set(new ProtossVsTerran)
  protoss.set(new ProtossVsProtoss)
  zerg.set(new ProtossVsZerg)
  random.set(new ProtossVsRandom)
}
