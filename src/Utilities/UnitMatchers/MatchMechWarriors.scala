package Utilities.UnitMatchers

object MatchMechWarriors extends MatchAnd(MatchWarriors, _.unitClass.isMechanical)
