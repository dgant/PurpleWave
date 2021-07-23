package Planning.UnitMatchers

object MatchFlyingWarriors extends MatchAnd(MatchWarriors, _.flying)