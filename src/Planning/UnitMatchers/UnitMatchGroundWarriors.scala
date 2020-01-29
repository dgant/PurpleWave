package Planning.UnitMatchers

object UnitMatchGroundWarriors extends UnitMatchAnd(UnitMatchWarriors, UnitMatchNot(UnitMatchMobileFlying))
