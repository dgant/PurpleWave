package Strategery.Selection

import Strategery.Strategies.Protoss._
import Strategery.{Heartbreak, MapGroups}

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  
  def add(opponent: Opponent): Opponent   = {
    allKnown = allKnown :+ opponent
    opponent
  }

  val pvzFFE1   = Seq(PvZFFEEconomic, PvZMidgame5GateGoonReaver,  PvZLateGameReaver)
  val pvzFFE2   = Seq(PvZFFEEconomic,                             PvZMidgameNeoBisu,          PvZLateGameTemplar)
  val pvz2Gate  = Seq(PvZ2Gate1012,   PvZ4GateGoon,               PvZMidgame5GateGoonReaver,  PvZLateGameReaver)
  val pvz1Gate  = Seq(PvZ10Gate,      PvZDT,                      PvZMidgameBisu,             PvZLateGameTemplar)

  val zergRotation = Vector(
    pvzFFE1,
    pvz2Gate,
    pvz1Gate,
    pvzFFE2,
    Seq(PvZ2Gate910,    PvZSpeedlot,  PvZMidgameBisu,        PvZLateGameTemplar))

  val defaultPvT  = StrategySelectionGreedy
  val defaultPvP  = StrategySelectionGreedy
  val defaultPvZ  = new StrategySelectionRecommended(StrategySelectionSequence(zergRotation), pvzFFE1: _*)
  val fixedPvT    = StrategySelectionFixed(PvT1015DT, PvT3BaseArbiter)
  val fixedPvZ    = StrategySelectionFixed(PvZ2Gate910, PvZ4GateGoon, PvZMidgame5GateGoon, PvZLateGameTemplar)
  val fixedPvR    = StrategySelectionFixed(PvR1BaseDT)
  val safePvZ     = new StrategySelectionRecommended(StrategySelectionGreedy, PvZ1BaseForgeTech, PvZMidgameNeoBisu, PvZLateGameTemplar)

  // AIST3 opponents
  val letabot           : Opponent = add(Opponent("Iron bot",           StrategySelectionFixed(PvT13Nexus, PvT3BaseCarrier)))

  // TorchUp opponents
  val bunkerBoxer   : Opponent = add(Opponent("BunkerBoxeR",  new StrategySelectionRecommended(StrategySelectionGreedy, PvT32Nexus, PvT3BaseArbiter)))
  val zzzkbot       : Opponent = add(Opponent("ZZZKBot",      safePvZ))
  val crona         : Opponent = add(Opponent("Crona",        StrategySelectionSequence(zergRotation)))
  val cunybot       : Opponent = add(Opponent("CUNYBot",      safePvZ))
  val ecgberht      : Opponent = add(Opponent("Ecgberht",     defaultPvT))
  val haopan        : Opponent = add(Opponent("Hao Pan",      new StrategySelectionRecommended(StrategySelectionGreedy, PvT32Nexus, PvT2BaseCarrier)))
  val locutus       : Opponent = add(Opponent("Locutus",      StrategySelectionSequence(Vector(
    Seq(PvP2GateDTExpand),
    Seq(PvP1ZealotExpand),
    Seq(PvPProxy2Gate)),
    loop = true)))
  val microwave     : Opponent = add(Opponent("Microwave",    StrategySelectionSequence(zergRotation)))
  val styxz         : Opponent = add(Opponent("StyxZ",        defaultPvZ))
  val tcpzerg       : Opponent = add(Opponent("TCPZerg",      safePvZ))

  // SSCAIT opponents
  val bananabrain   : Opponent = add(Opponent("BananaBrain",  defaultPvP))
  val killerbot     : Opponent = add(Opponent("Killerbot",    StrategySelectionFixed(PvZFFEEconomic, PvZMidgame5GateGoonReaver, PvZLateGameReaver)))
  val mariandevecka : Opponent = add(Opponent("Marian Devecka", killerbot.policy))
  val cherrypi      : Opponent = add(Opponent("CherryPiSSCAIT2017",
    if (MapGroups.badForProxying.exists(_.matches))
      StrategySelectionFixed(PvZ2Gate910, PvZSpeedlot, PvZMidgame5GateGoonReaver, PvZLateGameReaver)
    else
      StrategySelectionFixed(PvZProxy2Gate, PvZ2Gate910, PvZSpeedlot, PvZMidgame5GateGoonReaver, PvZLateGameReaver)))
  val dragon        : Opponent = add(Opponent("Dragon",
    //if (MapGroups.badForProxying.exists(_.matches))
      StrategySelectionFixed(PvT13Nexus, PvT3BaseArbiter)
    //else
    //  StrategySelectionFixed(PvTProxy2Gate, PvT1015Expand, PvT3BaseArbiter)
    ))
  val locutusClonePolicy = StrategySelectionSequence(Vector(
    Seq(PvP2Gate1012DT),
    Seq(PvPRobo),
    Seq(PvP2GateDTExpand),
    Seq(PvPProxy2Gate)))
  val znzzbot       : Opponent = add(Opponent("ZNZZBot",      locutusClonePolicy))
  val betastar      : Opponent = add(Opponent("BetaStar",     locutusClonePolicy))
  val daqin         : Opponent = add(Opponent("DaQin",        locutusClonePolicy))

  // AIIDE opponents
  val aitp          : Opponent = add(Opponent("AITP",         defaultPvT))
  val apollo        : Opponent = add(Opponent("Apollo",       defaultPvZ))
  val cdbot         : Opponent = add(Opponent("CDBot",        defaultPvZ))
  // dandan/daqin below
  val firefrog      : Opponent = add(Opponent("Firefrog",     defaultPvZ))
  val iron          : Opponent = add(Opponent("Iron bot",           StrategySelectionFixed(PvT1015DT, PvT2BaseCarrier)))
  val dandanbot     : Opponent = add(Opponent("DanDanBot",    locutus.policy))
  val mcrave        : Opponent = add(Opponent("McRave",       new StrategySelectionRecommended(StrategySelectionGreedy, PvPRobo) { duration = 1 }))
  val metabot       : Opponent = add(Opponent("MegaBot",      StrategySelectionFixed(PvPRobo)))
  val skynet        : Opponent = add(Opponent("Skynet",       metabot.policy)) // For testing purposes
  val andrewsmith   : Opponent = add(Opponent("Andrew Smith", metabot.policy)) // For testing purposes
  val aiur          : Opponent = add(Opponent("Aiur",         metabot.policy)) // For testing purposes
  val richoux       : Opponent = add(Opponent("Florian Richoux",metabot.policy))
  val vajda         : Opponent = add(Opponent("Tomas Vajda",  metabot.policy))
  val ximp          : Opponent = add(Opponent("XIMP",         metabot.policy))
  val murph         : Opponent = add(Opponent("Murph",        defaultPvP))
  val ophelia       : Opponent = add(Opponent("Ophelia",      defaultPvZ))
  val steamhammer   : Opponent = add(Opponent("Steamhammer",  defaultPvZ))
  val stormbreaker  : Opponent = add(Opponent("Stormbreaker", defaultPvT))
  val xiaoyi        : Opponent = add(Opponent("XiaoYi",       new StrategySelectionRecommended(StrategySelectionGreedy, PvT13Nexus, PvT2BaseCarrier) { duration = 3 }))

  // Returning AIIDE opponents
  val cse           : Opponent = add(Opponent("CSE",          (if (Heartbreak.matches) StrategySelectionFixed(PvP2Gate1012DT) else StrategySelectionFixed(PvP2GateDTExpand))))
  val saida         : Opponent = add(Opponent("SAIDA",        StrategySelectionFixed(PvT28Nexus, PvT2BaseCarrier)))
  val adias         : Opponent = add(Opponent("adias",        saida.policy))
  val ualbertabot   : Opponent = add(Opponent("UAlbertaBot",  fixedPvR))

  // Other tournaments

  val megabot       : Opponent = add(Opponent("MegaBot",      defaultPvP))
  //val aiur          : Opponent = add(Opponent("Aiur",         defaultPvP)) -- Disabled temporarily while testing MetaBot for AIIDE
  val tyr           : Opponent = add(Opponent("Tyr",          StrategySelectionFixed(PvP2GateDTExpand)))
  val overkill      : Opponent = add(Opponent("Overkill",     fixedPvZ))
  val ziabot        : Opponent = add(Opponent("Ziabot",       fixedPvZ))
  val opprimobot    : Opponent = add(Opponent("OpprimoBot",   new StrategySelectionRecommended(fixedPvR)))
  val bonjwa        : Opponent = add(Opponent("Bonjwa",       fixedPvT))
  val terranuab     : Opponent = add(Opponent("TerranUAB",    fixedPvT))
  val srbotone      : Opponent = add(Opponent("SRbotOne",     fixedPvT))
  val sling         : Opponent = add(Opponent("Sling",        fixedPvZ))
  val salsa         : Opponent = add(Opponent("Salsa",        fixedPvZ))
  val korean        : Opponent = add(Opponent("Korean",       defaultPvZ))
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
  val jadien            : Opponent = add(Opponent("jadien",             locutus.policy))
  
  val all: Vector[Opponent] = allKnown
}
