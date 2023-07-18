package Strategery.Selection

import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  private def add(name: String, policy: StrategySelectionPolicy = StrategySelectionGreedy()): Opponent = { val output = Opponent(name, policy); allKnown = allKnown :+ output; output }

  val defaultPvT = new StrategySelectionRecommended(StrategySelectionGreedy(), PvT28Nexus)
  val defaultPvP = StrategySelectionGreedy()
  val defaultPvZ = StrategySelectionGreedy()

  // COG 2023
  val bananabrain : Opponent = add("BananaBrain",   StrategySelectionGreedy())
  val stardust    : Opponent = add("Stardust",      new StrategySelectionRecommended(StrategySelectionGreedy(), PvPDT))
  val mcrave      : Opponent = add("McRave",        defaultPvZ)
  val microwave   : Opponent = add("Microwave",     defaultPvZ)
  val cunybot     : Opponent = add("CUNYbot",       defaultPvZ)
  val xiaoyi      : Opponent = add("XIAOYI",        defaultPvT)

  // AIIDE 2022
  val ualbertabot : Opponent = add("UAlbertaBot",   StrategySelectionFixed(PvR2Gate4Gate))

  // Aliased
  //val adias       : Opponent = add("adias")
  //val ualbertabot : Opponent = add("UAlbertaBot")
  //val zzzkbot     : Opponent = add("ZZZKBot")

  // Aliases
  val bryanweber        : Opponent = add("Bryan Weber",         cunybot.policy)
  val mcravez           : Opponent = add("McRaveZ",             mcrave.policy)
  //val styxz             : Opponent = add("StyxZ",               styx.policy)
  //val chriscoxe         : Opponent = add("Chris Coxe",          zzzkbot.policy)
  val davechurchill     : Opponent = add("Dave Churchill",      ualbertabot.policy)
  //val saida             : Opponent = add("SAIDA",               adias.policy)
  //val jadien            : Opponent = add("jadien",              adias.policy) // Local testing policy

  val all: Vector[Opponent] = allKnown
}
