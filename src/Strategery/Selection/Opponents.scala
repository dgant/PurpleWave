package Strategery.Selection

import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  private def add(name: String, policy: StrategySelectionPolicy = StrategySelectionGreedy()): Opponent = { val output = Opponent(name, policy); allKnown = allKnown :+ output; output }

  val defaultPvT = StrategySelectionFixed(PvT13Nexus, PvT2BaseReaver, PvT3BaseGateway)
  val defaultPvP = StrategySelectionFixed(PvPRobo, PvPRobo1Gate)
  val defaultPvZ = StrategySelectionFixed(PvZ2Gate1012, PvZ4GateGoon, PvZMidgame5GateGoon, PvZLateGameTemplar)

  // COG 2021 PREP
  val stardust    : Opponent = add("Stardust",    StrategySelectionFixed(PvPRobo, PvPRobo1012))
  val bananabrain : Opponent = add("BananaBrain", StrategySelectionFixed(PvPRobo, PvPRobo1012))
  val betastar    : Opponent = add("BetaStar",    StrategySelectionFixed(PvPRobo, PvPRobo1012))
  val metabot     : Opponent = add("MetaBot",     defaultPvP)
  val aiur        : Opponent = add("AIUR",        defaultPvP) // Metabot stand-in
  val skynet      : Opponent = add("Skynet",      defaultPvP) // Metabot stand-in
  val ximp        : Opponent = add("XIMP",        defaultPvP) // Metabot stand-in
  val xiaoyi      : Opponent = add("XIAOYI",      defaultPvT)
  val mcrave      : Opponent = add("McRave",      StrategySelectionFixed(PvZFFEEconomic, PvZMidgameCorsairReaverGoon, PvZLateGameReaver))
  val microwave   : Opponent = add("Microwave",   defaultPvZ)
  val cunybot     : Opponent = add("CUNYBot",     defaultPvZ)

  /*
  // AIST4
  val stardust    : Opponent = add("Stardust",    if (Medusa.matches) StrategySelectionFixed(PvP3GateGoon) else if (Eddy.matches) StrategySelectionFixed(PvP4GateGoon) else StrategySelectionFixed(PvPRobo1Gate, PvPRobo))
  val bananabrain : Opponent = add("BananaBrain", if (Medusa.matches) StrategySelectionFixed(PvP3GateGoon) else if (Eddy.matches) StrategySelectionFixed(PvP4GateGoon) else StrategySelectionFixed(PvPRobo1Gate, PvPRobo))
  val dragon      : Opponent = add("Dragon",      StrategySelectionFixed(PvT32Nexus, PvT3rdObs, PvT3BaseArbiter, PvEStormYes))
  val willyt      : Opponent = add("WillyT",      new StrategySelectionRecommended(StrategySelectionGreedy(), 2, PvT32Nexus, PvT2BaseReaver, PvT3BaseArbiter, PvEStormYes))
  val steamhammer : Opponent = add("Steamhammer", new StrategySelectionRecommended(StrategySelectionGreedy(), 2, PvZ2Gate1012, PvZ4GateGoon, PvZMidgame5GateGoonReaver, PvZLateGameReaver))

  // SSCAIT 2020-2021
  val monster     : Opponent = add("Monster",     new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ2Gate1012, PvZ4GateGoon, PvZMidgame5GateGoonReaver, PvZLateGameReaver) { duration = 3 })
  val haopan      : Opponent = add("Hao Pan",     new StrategySelectionRecommended(StrategySelectionGreedy(), PvT32Nexus, PvT2BaseCarrier, PvEStormNo)  { duration = 3 })
  val adias       : Opponent = add("adias",       StrategySelectionFixed(PvT24Nexus, PvT2BaseCarrier, PvEStormNo))
  val crona       : Opponent = add("Crona",       new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ2Gate1012, PvZ4GateGoon, PvZMidgame5GateGoonReaver, PvZLateGameReaver) { duration = 2 })
  val microwave   : Opponent = add("Microwave",   new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ2Gate1012, PvZ4GateGoon, PvZMidgame5GateGoonReaver, PvZLateGameReaver) { duration = 2 })
  val mcravez     : Opponent = add("McRaveZ",     new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ2Gate1012, PvZ4GateGoon, PvZMidgame5GateGoonReaver, PvZLateGameReaver) { duration = 2 })
  val iron        : Opponent = add("Iron",        StrategySelectionFixed(PvT24Nexus, PvT2BaseCarrier, PvEStormNo))
  */

  // AIIDE 2020
  val ecgberht    : Opponent = add("Ecgberht",    new StrategySelectionRecommended(StrategySelectionGreedy(), PvT32Nexus, PvT2BaseReaver, PvT3BaseGateway))
  val zzzkbot     : Opponent = add("ZZZKBot",     new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ1BaseForgeTech, PvZMidgameBisu, PvZLateGameTemplar))
  val daqin       : Opponent = add("DaQin",       new StrategySelectionRecommended(StrategySelectionGreedy(), PvPRobo))
  val ualbertabot : Opponent = add("UAlbertaBot", StrategySelectionFixed(PvR1BaseDT))

  // Aliased
  val iron        : Opponent = add("iron")
  val adias       : Opponent = add("adias")
  val letabot     : Opponent = add("LetaBot")
  val megabot     : Opponent = add("MegaBot")
  //val zzzkbot     : Opponent = add("ZZZKBot")
  //val ualbertabot : Opponent = add("UAlbertaBot")
  //val aiur        : Opponent = add("AIUR")
  val overkill    : Opponent = add("Overkill")
  val zia         : Opponent = add("Zia")
  val srbotone    : Opponent = add("SRBotOne")

  // Aliases

  val mcravez           : Opponent = add("McRaveZ",             mcrave.policy)
  val andrewsmith       : Opponent = add("Andrew Smith",        skynet.policy)
  val ironbot           : Opponent = add("Iron bot",            iron.policy)
  val martinrooijackers : Opponent = add("Martin Rooijackers",  letabot.policy)
  val megabot2017       : Opponent = add("MegaBot2017",         megabot.policy)
  val chriscoxe         : Opponent = add("Chris Coxe",          zzzkbot.policy)
  val davechurchill     : Opponent = add("Dave Churchill",      ualbertabot.policy)
  val florianrichoux    : Opponent = add("Florian Richoux",     aiur.policy)
  val saida             : Opponent = add("SAIDA",               adias.policy)
  val sijiaxu           : Opponent = add("Sijia Xu",            overkill.policy)
  val ziabot            : Opponent = add("Zia bot",             zia.policy)
  val johankayser       : Opponent = add("Johan Kayser",        srbotone.policy)
  val bryanweber        : Opponent = add("Bryan Weber",         cunybot.policy)
  val tomasvajda        : Opponent = add("Tomas Vajda",         ximp.policy)
  val jadien            : Opponent = add("jadien",              betastar.policy) // Local testing policy
  
  val all: Vector[Opponent] = allKnown
}
