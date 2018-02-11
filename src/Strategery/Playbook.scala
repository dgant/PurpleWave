package Strategery

import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss.PvT._
import Strategery.Strategies.Protoss.PvZ._
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Zerg.ZvT.TwoHatchMuta

class EmptyPlaybook {
  
  lazy val forced   : Seq[Strategy] = Seq.empty
  lazy val disabled : Seq[Strategy] = Seq.empty
  
  val none: Seq[Strategy] = Seq.empty
  
  val strategyOrder: Seq[Strategy] = Seq.empty
}

object StrategyGroups {
  val bad = Vector[Strategy](
    TvEProxy8Fact,
    TvEProxy5Rax,
    PvTEarly1GateProxy,
    PvT3BaseCorsair,
    CarriersWithNoDefense,
    ProxyDarkTemplar,
    PvZEarlyFFENexusFirst,
    PvZMidgame2Stargate
  )
}

class TestingPlaybook extends EmptyPlaybook {
  val strategiesToTest: Seq[Strategy] = Seq(TwoHatchMuta)
  
  //override lazy val forced: Seq[Strategy] = Seq(AllPvP, AllPvT, AllPvZ, TvTStandard, TvZStandard) ++ strategiesToTest
  override lazy val forced: Seq[Strategy] = strategiesToTest
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val forced    : Seq[Strategy] = none
  override lazy val disabled  : Seq[Strategy] = StrategyGroups.bad
}

object Playbook extends TestingPlaybook {}
