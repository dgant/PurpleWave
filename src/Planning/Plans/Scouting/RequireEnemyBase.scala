package Planning.Plans.Scouting

import Planning.Plans.Compound.{If, Not}

class RequireEnemyBase
  extends If (
    new Not(new FoundEnemyBase),
    new FindEnemyBase
  )