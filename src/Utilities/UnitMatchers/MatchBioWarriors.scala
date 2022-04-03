package Utilities.UnitMatchers

import ProxyBwapi.Races.Terran

object MatchBioWarriors extends MatchAnd(MatchOr(Terran.Marine, Terran.Firebat), _.aliveAndComplete, _.canMove)
