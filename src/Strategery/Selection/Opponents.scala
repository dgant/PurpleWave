package Strategery.Selection

import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  
  def add(opponent: Opponent): Opponent   = {
    allKnown = allKnown :+ opponent
    opponent
  }

  val defaultPvT  = new StrategySelectionRecommended(StrategySelectionGreedy, PvT28Nexus, PvT2BaseCarrier)
  val defaultPvP  = new StrategySelectionRecommended(StrategySelectionGreedy, PvP2Gate1012DT)
  val defaultPvZ  = new StrategySelectionRecommended(StrategySelectionGreedy, PvZ4Gate99, PvZMidgame5GateGoon, PvZLateGameTemplar)
  val fixedPvT    = new StrategySelectionFixed(PvT1015DT, PvT3BaseArbiter)
  val fixedPvZ    = new StrategySelectionFixed(PvZ4Gate99, PvZMidgame5GateGoon, PvZLateGameTemplar)
  val fixedPvR    = new StrategySelectionFixed(PvR1BaseDT)

  // New AIIDE opponents

  val aitp          : Opponent = add(Opponent("AITP",         defaultPvT))
  val apollo        : Opponent = add(Opponent("Apollo",       defaultPvZ))
  val bananabrain   : Opponent = add(Opponent("BananaBrain",  StrategySelectionRandom)) // TODO
  val bunkerBoxer   : Opponent = add(Opponent("BunkerBoxeR",  new StrategySelectionRecommended(StrategySelectionGreedy, PvT32Nexus, PvT3BaseArbiter)))
  val cdbot         : Opponent = add(Opponent("CDBot",        defaultPvZ))
  // dandan/daqin below
  val dragon        : Opponent = add(Opponent("Dragon",       StrategySelectionGreedy))
  val firefrog      : Opponent = add(Opponent("Firefrog",     defaultPvZ))
  val kimbot        : Opponent = add(Opponent("KimBot",       new StrategySelectionRecommended(StrategySelectionGreedy, PvTReaverCarrierCheese) { duration = 1 })) // IceLab or Leta fork?
  val letabot       : Opponent = add(Opponent("LetaBot",      new StrategySelectionRecommended(StrategySelectionGreedy, PvTReaverCarrierCheese)))
  val locutus       : Opponent = add(Opponent("Locutus",      StrategySelectionSequence(Vector(
    Seq(PvP2GateGoon),
    Seq(PvPRobo),
    Seq(PvP2Gate1012DT),
    Seq(PvP3GateGoon),
    Seq(PvP2GateDTExpand),
    Seq(PvPProxy2Gate)))))
  val dandanbot     : Opponent = add(Opponent("DanDanBot",    locutus.policy))
  val daqin         : Opponent = add(Opponent("DaQin",        locutus.policy))
  val mcrave        : Opponent = add(Opponent("McRave",       StrategySelectionGreedy))
  val metabot       : Opponent = add(Opponent("MegaBot",      new StrategySelectionFixed(PvPRobo)))
  val skynet        : Opponent = add(Opponent("Skynet",       metabot.policy))
  val andrewsmith   : Opponent = add(Opponent("Andrew Smith", metabot.policy))
  val aiur          : Opponent = add(Opponent("Aiur",         metabot.policy))
  val richoux       : Opponent = add(Opponent("Florian Richoux",metabot.policy))
  val vajda         : Opponent = add(Opponent("Tomas Vajda",  metabot.policy))
  val ximp          : Opponent = add(Opponent("XIMP",         metabot.policy))
  val microwave     : Opponent = add(Opponent("Microwave",    new StrategySelectionRecommended(StrategySelectionGreedy, PvZ4Gate99, PvZMidgame5GateGoon, PvZLateGameTemplar)))
  val murph         : Opponent = add(Opponent("Murph",        defaultPvP))
  val ophelia       : Opponent = add(Opponent("Ophelia",      defaultPvZ))
  val steamhammer   : Opponent = add(Opponent("Steamhammer",  StrategySelectionDynamic))
  val stormbreaker  : Opponent = add(Opponent("Stormbreaker", defaultPvT))
  val xiaoyi        : Opponent = add(Opponent("XiaoYi",       new StrategySelectionRecommended(StrategySelectionGreedy, PvT13Nexus, PvT2BaseCarrier) { duration = 3 }))

  // Returning AIIDE opponents
  val cse           : Opponent = add(Opponent("CSE",          StrategySelectionRandom)) // TODO
  val iron          : Opponent = add(Opponent("Iron",         new StrategySelectionFixed(PvT2GateRangeExpandCarrier)))
  val saida         : Opponent = add(Opponent("SAIDA",        new StrategySelectionFixed(PvT28Nexus, PvT2BaseCarrier)))
  val ualbertabot   : Opponent = add(Opponent("UAlbertaBot",  fixedPvR))
  val zzzkbot       : Opponent = add(Opponent("ZZZKBot",      new StrategySelectionFixed(PvZ1BaseForgeTechForced, PvZMidgameNeoBisu, PvZLateGameTemplar)))

  // Other tournaments

  val titaniron     : Opponent = add(Opponent("TitanIron",    new StrategySelectionRecommended(StrategySelectionGreedy, PvT2GateRangeExpandCarrier)))
  val megabot       : Opponent = add(Opponent("MegaBot",      defaultPvP))
  //val aiur          : Opponent = add(Opponent("Aiur",         defaultPvP)) -- Disabled temporarily while testing MetaBot for AIIDE
  val tyr           : Opponent = add(Opponent("Tyr",          new StrategySelectionFixed(PvP2GateDTExpand)))
  val ecgberht      : Opponent = add(Opponent("Ecgberht",     defaultPvT))
  val overkill      : Opponent = add(Opponent("Overkill",     fixedPvZ))
  val ziabot        : Opponent = add(Opponent("Ziabot",       fixedPvZ))
  val opprimobot    : Opponent = add(Opponent("OpprimoBot",   new StrategySelectionRecommended(fixedPvR)))
  val bonjwa        : Opponent = add(Opponent("Bonjwa",       fixedPvT))
  val terranuab     : Opponent = add(Opponent("TerranUAB",    fixedPvT))
  val srbotone      : Opponent = add(Opponent("SRbotOne",     fixedPvT))
  val sling         : Opponent = add(Opponent("Sling",        fixedPvZ))
  val salsa         : Opponent = add(Opponent("Salsa",        fixedPvZ))
  val korean        : Opponent = add(Opponent("Korean",       defaultPvZ))
  val cunybot       : Opponent = add(Opponent("CUNYBot",      new StrategySelectionRecommended(StrategySelectionGreedy, PvZFFEEconomic, PvZMidgame5GateGoonReaver, PvZLateGameReaver)))
  val isamind       : Opponent = add(Opponent("ISAMind",      locutus.policy))

  // Aliases

  val ironbot           : Opponent = add(Opponent("Iron bot",           iron.policy))
  val martinrooijackers : Opponent = add(Opponent("Martin Rooijackers", letabot.policy))
  val megabot2017       : Opponent = add(Opponent("MegaBot2017",        megabot.policy))
  val chriscoxe         : Opponent = add(Opponent("Chris Coxe",         zzzkbot.policy))
  val davechurchill     : Opponent = add(Opponent("Dave Churchill",     ualbertabot.policy))
  val florianrichoux    : Opponent = add(Opponent("Florian Richoux",    aiur.policy))
  val sijiaxu           : Opponent = add(Opponent("Sijia Xu",           overkill.policy))
  val ziabot2           : Opponent = add(Opponent("Zia bot",            ziabot.policy))
  val johankayser       : Opponent = add(Opponent("Johan Kayser",       srbotone.policy))
  val bryanweber        : Opponent = add(Opponent("Bryan Weber",        cunybot.policy))
  val jadien            : Opponent = add(Opponent("jadien",             StrategySelectionRandom))
  
  val all: Vector[Opponent] = allKnown
}
