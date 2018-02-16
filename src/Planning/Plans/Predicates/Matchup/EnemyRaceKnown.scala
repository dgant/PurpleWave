package Planning.Plans.Predicates.Matchup

import Planning.Plans.Compound.Or

class EnemyRaceKnown extends Or(
  new EnemyIsTerran,
  new EnemyIsProtoss,
  new EnemyIsZerg)
