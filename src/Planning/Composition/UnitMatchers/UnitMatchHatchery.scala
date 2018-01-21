package Planning.Composition.UnitMatchers

import ProxyBwapi.Races.Zerg

object UnitMatchHatchery extends UnitMatchOr(Zerg.Hatchery, Zerg.Lair, Zerg.Hive)
