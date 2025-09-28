package Strategery.Selection

import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  private def add(opponent: Opponent): Opponent = { allKnown = allKnown :+ opponent; opponent }
  private def add(name: String, targetWinrate: Double, policy: StrategySelectionPolicy = StrategySelectionGreedy()): Opponent = add(Opponent(name, targetWinrate, policy))
  private def add(name: String, other: Opponent): Opponent = add(new Opponent(name, other))

  val aggroPvZ  : StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ1BaseReactive, PvZSpeedlot, PvZMuscle) { duration = 10 }
  val ecoPvZ    : StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvZFFE) { duration = 10 }
  val defaultPvT: StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvTZZCoreZ)
  val defaultPvP: StrategySelectionPolicy = StrategySelectionGreedy()
  val defaultPvZ: StrategySelectionPolicy = aggroPvZ
  val defaultPvR: StrategySelectionPolicy = StrategySelectionFixed(PvR2Gate4Gate)

  // AIIDE 2025

  // AIIDE 2024
  val bananabrain : Opponent = add("BananaBrain",     .6,   new StrategySelectionRecommended(StrategySelectionGreedy(), PvP1012, PvP5Zealot, PvPDT) { duration = 3 })
  val stardust    : Opponent = add("Stardust",        .75,  new StrategySelectionRecommended(StrategySelectionGreedy(), PvPGateCore, PvPDT) { duration = 6 })
  val mcrave      : Opponent = add("McRave",          .6,   StrategySelectionFixed(PvZ1BaseReactive, PvZTech, PvZSpeedlot))
  val microwave   : Opponent = add("Microwave",       .9,   aggroPvZ)
  val infestedart : Opponent = add("InfestedArtosis", .9,   aggroPvZ)
  val steamhammer : Opponent = add("Steamhammer",     .9,   aggroPvZ)
  val ualbertabot : Opponent = add("UAlbertaBot",     .95,  defaultPvR)

  val insanitybot : Opponent = add("InsanityBot",     .95,  defaultPvT)
  val void        : Opponent = add("Void",            .95,  defaultPvT)
  val nitekat     : Opponent = add("NiteKat",         .95,  defaultPvT)


  // Aliased
  //val adias       : Opponent = add("adias")
  //val cunybot     : Opponent = add("CUNYbot")
  //val ualbertabot : Opponent = add("UAlbertaBot")
  //val zzzkbot     : Opponent = add("ZZZKBot")

  // Aliases
  val mcravez       : Opponent = add("McRaveZ",         mcrave)
  val davechurchill : Opponent = add("Dave Churchill",  ualbertabot)
  //val bryanweber    : Opponent = add("Bryan Weber",     cunybot)
  //val styxz         : Opponent = add("StyxZ",           styx)
  //val chriscoxe     : Opponent = add("Chris Coxe",      zzzkbot)
  //val saida         : Opponent = add("SAIDA",           adias)
  //val jadien        : Opponent = add("jadien",          adias) // Local testing policy

  val all: Vector[Opponent] = allKnown
}
