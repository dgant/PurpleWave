package Strategery.Selection

import Strategery.{Eddy, Medusa}
import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  private def add(name: String, policy: StrategySelectionPolicy = StrategySelectionGreedy()): Opponent = { val output = Opponent(name, policy); allKnown = allKnown :+ output; output }

  val defaultPvT = new StrategySelectionRecommended(StrategySelectionGreedy(), PvT32Nexus, PvT2BaseReaver, PvT3BaseArbiter)
  val defaultPvP = new StrategySelectionRecommended(StrategySelectionGreedy(), PvPRobo, PvP3rdBaseSlow)
  val defaultPvZ = new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ2Gate910, PvZ4GateGoon, PvZMidgame5GateGoonReaver, PvZLateGameTemplar)

  // AIST4
  val stardust    : Opponent = add("Stardust",    if (Medusa.matches || Eddy.matches) StrategySelectionFixed(PvP3GateGoon, PvP3rdBaseSlow) else StrategySelectionFixed(PvPRobo1Gate, PvPRobo, PvP3rdBaseSlow))
  val bananabrain : Opponent = add("BananaBrain", if (Medusa.matches || Eddy.matches) StrategySelectionFixed(PvP3GateGoon, PvP3rdBaseSlow) else StrategySelectionFixed(PvPRobo1Gate, PvPRobo, PvP3rdBaseSlow))
  val dragon      : Opponent = add("Dragon",      new StrategySelectionRecommended(StrategySelectionGreedy(), 22, PvT32Nexus, PvT3rdObs, PvT3BaseGateway, PvEStormNo))
  val willyt      : Opponent = add("WillyT",      new StrategySelectionRecommended(StrategySelectionGreedy(), 22, PvT32Nexus, PvT2BaseReaver, PvT3BaseArbiter))
  val steamhammer : Opponent = add("Steamhammer", new StrategySelectionRecommended(StrategySelectionGreedy(), 22, PvZ2Gate1012, PvZ4GateGoon, PvZMidgame5GateGoonReaver, PvZLateGameReaver))

  // SSCAIT 2020-2021
  val monster     : Opponent = add("Monster",     new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ2Gate1012, PvZ4GateGoon, PvZMidgame5GateGoonReaver, PvZLateGameReaver) { duration = 3 })
  val haopan      : Opponent = add("Hao Pan",     new StrategySelectionRecommended(StrategySelectionGreedy(), PvT32Nexus, PvT2BaseCarrier, PvEStormNo)  { duration = 3 })
  val adias       : Opponent = add("adias",       StrategySelectionFixed(PvT24Nexus, PvT2BaseCarrier, PvEStormNo))
  val crona       : Opponent = add("Crona",       new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ2Gate1012, PvZ4GateGoon, PvZMidgame5GateGoonReaver, PvZLateGameReaver) { duration = 2 })
  val microwave   : Opponent = add("Microwave",   new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ2Gate1012, PvZ4GateGoon, PvZMidgame5GateGoonReaver, PvZLateGameReaver) { duration = 2 })
  val mcravez     : Opponent = add("McRaveZ",     new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ2Gate1012, PvZ4GateGoon, PvZMidgame5GateGoonReaver, PvZLateGameReaver) { duration = 2 })
  val iron        : Opponent = add("Iron",        StrategySelectionFixed(PvT24Nexus, PvT2BaseCarrier, PvEStormNo))

  // AIIDE 2020
  val dandanbot   : Opponent = add("DanDanBot",   defaultPvP)
  val ecgberht    : Opponent = add("Ecgberht",    new StrategySelectionRecommended(StrategySelectionGreedy(), PvT32Nexus, PvT2BaseReaver, PvT3BaseGateway))
  val eggbot      : Opponent = add("EggBot",      defaultPvP)
  val randofoo    : Opponent = add("Randofoo",    defaultPvP)

  val taij        : Opponent = add("Taij",        defaultPvT)
  val zzzkbot     : Opponent = add("ZZZKBot",     new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ1BaseForgeTech, PvZMidgameBisu, PvZLateGameTemplar))
  val daqin       : Opponent = add("DaQin",       new StrategySelectionRecommended(StrategySelectionGreedy(), PvPRobo, PvP3rdBaseSlow))
  val ualbertabot : Opponent = add("UAlbertaBot", StrategySelectionFixed(PvR1BaseDT))

  // COG 2020
  val metabot     : Opponent = add("MetaBot",     StrategySelectionFixed(PvPRobo))
  val betastar    : Opponent = add("BetaStar")
  val xiaoyi      : Opponent = add("XIAOYI",      StrategySelectionFixed(PvT13Nexus, PvT2BaseCarrier))
  val aiur        : Opponent = add("AIUR",        StrategySelectionFixed(PvPRobo)) // Metabot stand-in
  val skynet      : Opponent = add("Skynet",      StrategySelectionFixed(PvPRobo)) // Metabot stand-in
  val ximp        : Opponent = add("XIMP",        StrategySelectionFixed(PvPRobo)) // Metabot stand-in

  // Aliased
  val letabot     : Opponent = add("LetaBot")
  val megabot     : Opponent = add("MegaBot")
  //val zzzkbot     : Opponent = add("ZZZKBot")
  //val ualbertabot : Opponent = add("UAlbertaBot")
  //val aiur        : Opponent = add("AIUR")
  val overkill    : Opponent = add("Overkill")
  val zia         : Opponent = add("Zia")
  val srbotone    : Opponent = add("SRBotOne")
  val cunybot     : Opponent = add("CUNYBot")

  // Aliases
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
  val jadien            : Opponent = add("jadien",              stardust.policy) // Local testing policy
  
  val all: Vector[Opponent] = allKnown
}
