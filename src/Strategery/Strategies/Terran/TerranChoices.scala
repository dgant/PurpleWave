package Strategery.Strategies.Terran

import Strategery.Strategies.Terran.FFA.TerranFFABio
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Terran.TvR.{TvR1Rax, TvRTinfoil}
import Strategery.Strategies.Terran.TvZ.TvZProxy8Fact
import Strategery.Strategies._

object TerranChoices {
  
  /////////
  // TvR //
  /////////
  
  val tvr = Vector(
    TvEWorkerRushLiftoff,
    TvEProxy5Rax,
    TvEProxyBBS,
    TvE1RaxSCVMarine,
    TvE2RaxSCVMarine,
    TvR1Rax,
    TvRTinfoil,
    TerranFFABio)
  
  /////////
  // TvT //
  /////////
  
  val tvtOpeners = Vector(
    TvT14CC,
    TvT1RaxFE,
    TvT1FacFE,
    TvT1FacPort,
    TvT2FacTanks,
    TvT2Port
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
    TvZ1RaxFE,
    TvZ2RaxAcademy,
    TvZ8Rax,
    TvZProxy8Fact,
  )
  
  val normalOpeners: Vector[Strategy] = (tvr ++ tvtOpeners ++ tvpOpeners ++ tvzOpeners).distinct
  
  /////////
  // All //
  /////////
  
  val all: Vector[Strategy] = (normalOpeners).distinct
}