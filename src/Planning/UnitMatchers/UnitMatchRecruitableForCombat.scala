package Planning.UnitMatchers

import ProxyBwapi.Races.Zerg

object UnitMatchRecruitableForCombat extends UnitMatchAnd(
  UnitMatchMobile,
  UnitMatchNot(UnitMatchWorkers),
  UnitMatchNot(Zerg.Larva))
