package Planning.UnitMatchers

import ProxyBwapi.Races.Zerg

object MatchHatchlike extends MatchOr(Zerg.Hatchery, Zerg.Lair, Zerg.Hive)
