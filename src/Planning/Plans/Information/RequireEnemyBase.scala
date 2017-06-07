package Planning.Plans.Information

import Planning.Plans.Compound.{IfThenElse, Not}

class RequireEnemyBase
  extends IfThenElse (
    new Not(new FoundEnemyBase),
    new FindEnemyBase
  ) {
  
  description.set("Require a known enemy base")
}
