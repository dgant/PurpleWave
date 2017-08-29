package Planning.Composition.UnitMatchers

import ProxyBwapi.Races.Terran

object UnitMatchSiegeTank extends UnitMatchOr(Terran.SiegeTankUnsieged, Terran.SiegeTankSieged)
