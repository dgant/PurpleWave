package Planning.UnitMatchers

object MatchMechWarriors extends MatchAnd(MatchWarriors, _.unitClass.isMechanical)
