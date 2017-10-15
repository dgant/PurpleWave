package Planning.Plans.Information.Matchup

import Planning.Plans.Compound.Or

class EnemyRaceKnown extends Or(
  new EnemyIsTerran,
  new EnemyIsProtoss,
  new EnemyIsZerg)
