package Planning.UnitMatchers

import ProxyBwapi.Races.Zerg

object MatchRecruitableForCombat extends MatchAnd(
  MatchMobile,
  MatchNot(MatchWorkers),
  MatchNot(Zerg.Larva))
