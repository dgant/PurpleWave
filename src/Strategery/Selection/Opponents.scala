package Strategery.Selection

import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  private def add(name: String, policy: StrategySelectionPolicy = StrategySelectionGreedy()): Opponent = { val output = Opponent(name, policy); allKnown = allKnown :+ output; output }

  val defaultPvT = new StrategySelectionRecommended(StrategySelectionGreedy(), PVT910, PvTMidgameCarrier, PvTEndgameCarrier)
  val defaultPvP = StrategySelectionGreedy()
  val defaultPvZ = StrategySelectionGreedy()

  // AIIDE  2022
  val bananabrain : Opponent = add("BananaBrain",   StrategySelectionGreedy())
  val stardust    : Opponent = add("Stardust",      new StrategySelectionRecommended(StrategySelectionGreedy(), PvPDT,PvPGateCoreTech))
  val mcrave      : Opponent = add("McRave",        defaultPvZ)
  val microwave   : Opponent = add("Microwave",     defaultPvZ)
  val steamhammer : Opponent = add("Steamhammer",   defaultPvZ)
  val cunybot     : Opponent = add("CUNYbot",       defaultPvZ)
  val styx        : Opponent = add("Styx",          defaultPvZ)
  val dragon      : Opponent = add("Dragon",        defaultPvT)
  val pylonpuller : Opponent = add("PylonPuller",   defaultPvP)
  val ualbertabot : Opponent = add("UAlbertaBot",   StrategySelectionFixed(PvR2Gate4Gate))

  // COG 2022
  val betastar    : Opponent = add("BetaStar",      StrategySelectionFixed(PvPRobo, PvPGateCoreTech))
  val xiaoyi      : Opponent = add("XIAOYI",        defaultPvT)

  // Aliased
  //val adias       : Opponent = add("adias")
  //val ualbertabot : Opponent = add("UAlbertaBot")
  //val zzzkbot     : Opponent = add("ZZZKBot")

  // Aliases
  val bryanweber        : Opponent = add("Bryan Weber",         cunybot.policy)
  val mcravez           : Opponent = add("McRaveZ",             mcrave.policy)
  val styxz             : Opponent = add("StyxZ",               styx.policy)
  //val chriscoxe         : Opponent = add("Chris Coxe",          zzzkbot.policy)
  val davechurchill     : Opponent = add("Dave Churchill",      ualbertabot.policy)
  //val saida             : Opponent = add("SAIDA",               adias.policy)
  //val jadien            : Opponent = add("jadien",              adias.policy) // Local testing policy

  val all: Vector[Opponent] = allKnown
}
