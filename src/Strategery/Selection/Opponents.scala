package Strategery.Selection

import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  private def add(name: String, policy: StrategySelectionPolicy = StrategySelectionGreedy()): Opponent = { val output = Opponent(name, policy); allKnown = allKnown :+ output; output }

  val aggroPvZ  : StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ1BaseReactive, PvZSpeedlot, PvZMuscle) { duration = 10 }
  val ecoPvZ    : StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvZFFE) { duration = 10 }
  val defaultPvT: StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvTZZCoreZ)
  val defaultPvP: StrategySelectionPolicy = StrategySelectionGreedy()
  val defaultPvZ: StrategySelectionPolicy = aggroPvZ

  // AIIDE 2024
  val bananabrain : Opponent = add("BananaBrain",     new StrategySelectionRecommended(StrategySelectionGreedy(), PvPGateCore, PvPRobo, PvPReaver))
  val stardust    : Opponent = add("Stardust",        new StrategySelectionRecommended(StrategySelectionGreedy(), PvPGateCore, PvPDT))
  val steamhammer : Opponent = add("Steamhammer",     aggroPvZ)
  val microwave   : Opponent = add("Microwave",       aggroPvZ)
  val infestedart : Opponent = add("InfestedArtosis", aggroPvZ)
  val ualbertabot : Opponent = add("UAlbertaBot",     StrategySelectionFixed(PvR2Gate4Gate))
  val mcrave      : Opponent = add("McRave",          StrategySelectionGreedy())

  // COG 2023
  val xiaoyi      : Opponent = add("XIAOYI",        StrategySelectionFixed(PvT28Nexus))

  // Aliased
  //val adias       : Opponent = add("adias")
  val cunybot       : Opponent = add("CUNYbot")
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
