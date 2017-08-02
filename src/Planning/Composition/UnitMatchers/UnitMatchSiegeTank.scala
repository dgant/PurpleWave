package Planning.Composition.UnitMatchers

import ProxyBwapi.Races.Terran

object UnitMatchSiegeTank extends UnitMatchOr(
  UnitMatchType(Terran.SiegeTankUnsieged),
  UnitMatchType(Terran.SiegeTankSieged))
