package Strategery.Strategies.Terran

import Strategery.Strategies.Terran.FFA.TerranFFABio
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Terran.TvR.{TvR1Rax, TvRTinfoil}
import Strategery.Strategies.Terran.TvT.TvTStandard
import Strategery.Strategies.Terran.TvZ._
import Strategery.Strategies._

object TerranChoices {
  
  /////////
  // TvR //
  /////////
  
  val tvr = Vector(
    WorkerRushLiftoff,
    TvEProxy5Rax,
    TvEProxy8Fact,
    TvEProxyBBS,
    TvE2RaxSCVMarine,
    TvEMassBio,
    TvETurtleMech,
    TvR1Rax,
    TvRTinfoil,
    TvEMassGoliath,
    TvE2PortWraith,
    TerranFFABio)
  
  /////////
  // TvT //
  /////////
  
  val tvtOpeners = Vector(
    TvTStandard
    //TvTPNukeDrop
  )
  
  /////////
  // TvP //
  /////////
  
  val tvpOpeners = Vector(
    TvP1RaxFE,
    TvPSiegeExpandBunker,
    TvPFDStrong,
    TvP2FacJoyO,
  )
  
  /////////
  // TvZ //
  /////////
  
  val tvzOpeners = Vector(
    TvZEarlyCCFirst,
    TvZEarly1RaxGas,
    TvZEarly1RaxFEEconomic,
    TvZEarly1RaxFEConservative,
    TvZEarly2Rax
  )
  
  val normalOpeners: Vector[Strategy] = (tvr ++ tvtOpeners ++ tvpOpeners ++ tvzOpeners).distinct
  
  /////////
  // All //
  /////////
  
  val all: Vector[Strategy] = (normalOpeners).distinct
}