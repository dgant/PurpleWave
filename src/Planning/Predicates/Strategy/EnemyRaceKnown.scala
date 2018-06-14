package Planning.Predicates.Strategy

import Planning.Plans.Compound.Or

class EnemyRaceKnown extends Or(
  new EnemyIsTerran,
  new EnemyIsProtoss,
  new EnemyIsZerg)
