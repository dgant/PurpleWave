package Plans.Information

import Plans.Compound.Until

class KnowEnemyBase extends Until {
  predicate.set(new FoundEnemyBase)
  action.set(new FindEnemyBase)
}
