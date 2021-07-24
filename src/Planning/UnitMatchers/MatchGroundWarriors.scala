package Planning.UnitMatchers

object MatchGroundWarriors extends MatchAnd(MatchWarriors, ! _.flying)
