package Plans.Information

import Plans.Compound.IfThenElse

class RequireEnemyBasePosition extends IfThenElse {
  predicate.set(new FoundEnemyBase)
  whenFalse.set(new FindEnemyBase)
}
