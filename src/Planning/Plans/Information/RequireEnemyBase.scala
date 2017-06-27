package Planning.Plans.Information

import Planning.Plans.Compound.{If, Not}

class RequireEnemyBase
  extends If (
    new Not(new FoundEnemyBase),
    new FindEnemyBase
  )