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

  // AIIDE 2024
  val bananabrain : Opponent = add("BananaBrain",     .6,   new StrategySelectionRecommended(StrategySelectionGreedy(), PvPGateCore, PvPRobo, PvPReaver))
  val stardust    : Opponent = add("Stardust",        .6,   new StrategySelectionRecommended(StrategySelectionGreedy(), PvPGateCore, PvPDT))
  val mcrave      : Opponent = add("McRave",          .6,   StrategySelectionGreedy())
  val microwave   : Opponent = add("Microwave",       .9,   aggroPvZ)
  val insanitybot : Opponent = add("InsanityBot",     .9,   defaultPvT)
  val infestedart : Opponent = add("InfestedArtosis", .9,   aggroPvZ)
  val steamhammer : Opponent = add("Steamhammer",     .9,   aggroPvZ)
  val ualbertabot : Opponent = add("UAlbertaBot",     .9,   StrategySelectionFixed(PvR2Gate4Gate))

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
