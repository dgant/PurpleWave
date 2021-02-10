package Planning.UnitMatchers

object MatchGroundWarriors extends MatchAnd(MatchWarriors, MatchNot(MatchMobileFlying))
