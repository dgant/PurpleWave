package Plans.Information

import Plans.Compound.IfThenElse

class KnowEnemyBase extends IfThenElse {
  predicate.set(new FoundEnemyBase)
  whenFalse.set(new FindEnemyBase)
}
