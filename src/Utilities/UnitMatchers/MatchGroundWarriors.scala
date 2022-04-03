package Utilities.UnitMatchers

object MatchGroundWarriors extends MatchAnd(MatchWarriors, ! _.flying)
