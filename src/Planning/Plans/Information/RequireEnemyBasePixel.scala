package Planning.Plans.Information

import Planning.Plans.Compound.IfThenElse

class RequireEnemyBasePixel extends IfThenElse {
  predicate.set(new FoundEnemyBase)
  whenFalse.set(new FindEnemyBase)
}
