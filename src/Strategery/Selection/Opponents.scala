package Strategery.Selection

import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  private def add(name: String, policy: StrategySelectionPolicy = StrategySelectionGreedy()): Opponent = { val output = Opponent(name, policy); allKnown = allKnown :+ output; output }

  val defaultPvT = StrategySelectionFixed(PvTZZCoreZ, PvT2BaseReaver, PvT3BaseGateway, PvEStormYes)
  val defaultPvP = new StrategySelectionRecommended(StrategySelectionGreedy(), PvPRobo, PvPGateCoreGate)
  val defaultPvZ = StrategySelectionFixed(PvZ2GateFlex)

  // AIIDE 2021
  // Terran
  val dragon      : Opponent = add("Dragon",      defaultPvT)
  val willyt      : Opponent = add("WillyT",      new StrategySelectionRecommended(StrategySelectionGreedy(), PvTZZCoreZ, PvT2BaseReaver, PvT3BaseGateway, PvEStormYes))
  val taiji       : Opponent = add("Taiji",       new StrategySelectionRecommended(StrategySelectionGreedy(), PvTZZCoreZ, PvT2BaseReaver, PvT3BaseGateway, PvEStormYes))
  // Protoss
  val bananabrain : Opponent = add("BananaBrain", StrategySelectionGreedy())
  val stardust    : Opponent = add("Stardust",    StrategySelectionGreedy())
  val daqin       : Opponent = add("DaQin",       new StrategySelectionRecommended(StrategySelectionGreedy(), PvPRobo, PvPGateCoreGate))
  val bluesoup    : Opponent = add("Bluesoup",    defaultPvP)
  // Zerg
  val steamhammer : Opponent = add("Steamhammer", defaultPvZ)
  val mcrave      : Opponent = add("McRave",      defaultPvZ)
  val freshmeat   : Opponent = add("FreshMeat",   defaultPvZ)
  val microwave   : Opponent = add("Microwave",   defaultPvZ)
  val real5drone  : Opponent = add("real5Drone",  defaultPvZ) //new StrategySelectionRecommended(defaultPvZ, PvZ1BaseForgeTech, PvZMidgameBisu, PvZLateGameTemplar))
  val crona       : Opponent = add("Crona",       freshmeat.policy)
  val zzzkbot     : Opponent = add("ZZZKBot",     real5drone.policy)
  // Random
  val ualbertabot : Opponent = add("UAlbertaBot", StrategySelectionFixed(PvR1BaseDT))

  /*
  // COG 2021
  private val cogFixedPvP = StrategySelectionFixed(PvPRobo, PvPGateCoreGate)
  val betastar    : Opponent = add("BetaStar",    cogFixedPvP)
  val metabot     : Opponent = add("MetaBot",     cogFixedPvP)
  val aiur        : Opponent = add("AIUR",        cogFixedPvP) // Metabot stand-in
  val skynet      : Opponent = add("Skynet",      cogFixedPvP) // Metabot stand-in
  val ximp        : Opponent = add("XIMP",        cogFixedPvP) // Metabot stand-in
  val xiaoyi      : Opponent = add("XIAOYI",      defaultPvT)
  */

  /*
  // AIST4
  val stardust    : Opponent = add("Stardust",    if (Medusa.matches) StrategySelectionFixed(PvP3GateGoon) else if (Eddy.matches) StrategySelectionFixed(PvP4GateGoon) else StrategySelectionFixed(PvPRobo1Gate, PvPRobo))
  val bananabrain : Opponent = add("BananaBrain", if (Medusa.matches) StrategySelectionFixed(PvP3GateGoon) else if (Eddy.matches) StrategySelectionFixed(PvP4GateGoon) else StrategySelectionFixed(PvPRobo1Gate, PvPRobo))
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
  //val ecgberht    : Opponent = add("Ecgberht",    new StrategySelectionRecommended(StrategySelectionGreedy(), PvT32Nexus, PvT2BaseReaver, PvT3BaseGateway))
  //val zzzkbot     : Opponent = add("ZZZKBot",     new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ1BaseForgeTech, PvZMidgameBisu, PvZLateGameTemplar))

  // Aliased
  val adias       : Opponent = add("adias")
  //val ualbertabot : Opponent = add("UAlbertaBot")
  //val zzzkbot     : Opponent = add("ZZZKBot")

  // Aliases
  val mcravez           : Opponent = add("McRaveZ",             mcrave.policy)
  val chriscoxe         : Opponent = add("Chris Coxe",          zzzkbot.policy)
  val davechurchill     : Opponent = add("Dave Churchill",      ualbertabot.policy)
  val saida             : Opponent = add("SAIDA",               adias.policy)
  val jadien            : Opponent = add("jadien",              adias.policy) // Local testing policy
  
  val all: Vector[Opponent] = allKnown
}
