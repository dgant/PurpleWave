package Utilities.UnitMatchers

import ProxyBwapi.Races.{Protoss, Zerg}

object MatchRecruitableForCombat extends MatchAnd(
  MatchMobile,
  MatchNot(MatchWorker, Protoss.Interceptor, Protoss.Scarab, Zerg.Larva))
