package Planning.UnitMatchers

import ProxyBwapi.Races.{Protoss, Zerg}

object MatchRecruitableForCombat extends MatchAnd(
  MatchMobile,
  MatchNot(MatchWorkers, Protoss.Interceptor, Protoss.Scarab, Zerg.Larva))
