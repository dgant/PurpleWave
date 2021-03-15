package Planning.UnitMatchers

import ProxyBwapi.Races.Terran

object MatchTank extends MatchOr(Terran.SiegeTankUnsieged, Terran.SiegeTankSieged)
