package Strategery.Selection

import Strategery.{Benzene, Heartbreak, MapGroups}
import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  
  def add(opponent: Opponent): Opponent   = {
    allKnown = allKnown :+ opponent
    opponent
  }

  val defaultPvT  = StrategySelectionGreedy
  val defaultPvP  = StrategySelectionGreedy
  val defaultPvZ  = StrategySelectionGreedy
  val fixedPvT    = StrategySelectionFixed(PvT1015DT, PvT3BaseArbiter)
  val fixedPvZ    = StrategySelectionFixed(PvZ4Gate99, PvZMidgame5GateGoon, PvZLateGameTemplar)
  val fixedPvR    = StrategySelectionFixed(PvR1BaseDT)

  // Recently updated opponents
  val bananabrain   : Opponent = add(Opponent("BananaBrain",  defaultPvP))
  val styxz         : Opponent = add(Opponent("StyxZ",
    (if (Benzene.matches)
    new StrategySelectionRecommended(StrategySelectionGreedy, PvZ4Gate99, PvZMidgame5GateGoon, PvZLateGameTemplar) { duration = 2 } else
    new StrategySelectionRecommended(StrategySelectionGreedy, PvZ1BaseForgeTechForced, PvZMidgameNeoBisu, PvZLateGameTemplar) { duration = 3 }
  )))
  val haopan        : Opponent = add(Opponent("Hao Pan",      defaultPvT))
  val killerbot     : Opponent = add(Opponent("Killerbot",    StrategySelectionFixed(PvZFFEEconomic, PvZMidgame5GateGoonReaver, PvZLateGameReaver)))
  val mariandevecka : Opponent = add(Opponent("Marian Devecka", killerbot.policy))
  val cherrypi      : Opponent = add(Opponent("CherryPiSSCAIT2017",
    if (MapGroups.badForProxying.exists(_.matches))
      StrategySelectionFixed(PvZ4Gate99, PvZMidgame5GateGoonReaver, PvZLateGameReaver)
    else
      StrategySelectionFixed(PvZProxy2Gate, PvZ4Gate99, PvZMidgame5GateGoonReaver, PvZLateGameReaver)))
  val dragon        : Opponent = add(Opponent("Dragon",
    //if (MapGroups.badForProxying.exists(_.matches))
      StrategySelectionFixed(PvT13Nexus, PvT3BaseArbiter)
    //else
    //  StrategySelectionFixed(PvTProxy2Gate, PvT1015Expand, PvT3BaseArbiter)
    ))
  val locutus       : Opponent = add(Opponent("Locutus",      StrategySelectionSequence(Vector(
    Seq(PvP2Gate1012DT),
    Seq(PvPRobo),
    Seq(PvP2GateDTExpand),
    Seq(PvP3GateGoon)))))
  val znzzbot       : Opponent = add(Opponent("ZNZZBot",      locutus.policy))
  val betastar      : Opponent = add(Opponent("BetaStar",     locutus.policy))
  val daqin         : Opponent = add(Opponent("DaQin",        locutus.policy))

  // AIIDE opponents
  val aitp          : Opponent = add(Opponent("AITP",         defaultPvT))
  val apollo        : Opponent = add(Opponent("Apollo",       defaultPvZ))
  val bunkerBoxer   : Opponent = add(Opponent("BunkerBoxeR",  new StrategySelectionRecommended(StrategySelectionGreedy, PvT32Nexus, PvT3BaseArbiter)))
  val cdbot         : Opponent = add(Opponent("CDBot",        defaultPvZ))
  // dandan/daqin below
  val firefrog      : Opponent = add(Opponent("Firefrog",     defaultPvZ))
  val kimbot        : Opponent = add(Opponent("KimBot",       new StrategySelectionRecommended(StrategySelectionGreedy, PvTReaverCarrierCheese) { duration = 1 })) // IceLab or Leta fork?
  val letabot       : Opponent = add(Opponent("LetaBot",      new StrategySelectionRecommended(StrategySelectionGreedy, PvTReaverCarrierCheese)))
  val dandanbot     : Opponent = add(Opponent("DanDanBot",    locutus.policy))
  val mcrave        : Opponent = add(Opponent("McRave",       new StrategySelectionRecommended(StrategySelectionGreedy, PvPRobo) { duration = 1 }))
  val metabot       : Opponent = add(Opponent("MegaBot",      StrategySelectionFixed(PvPRobo)))
  val skynet        : Opponent = add(Opponent("Skynet",       metabot.policy)) // For testing purposes
  val andrewsmith   : Opponent = add(Opponent("Andrew Smith", metabot.policy)) // For testing purposes
  val aiur          : Opponent = add(Opponent("Aiur",         metabot.policy)) // For testing purposes
  val richoux       : Opponent = add(Opponent("Florian Richoux",metabot.policy))
  val vajda         : Opponent = add(Opponent("Tomas Vajda",  metabot.policy))
  val ximp          : Opponent = add(Opponent("XIMP",         metabot.policy))
  val microwave     : Opponent = add(Opponent("Microwave",    defaultPvZ))
  val murph         : Opponent = add(Opponent("Murph",        defaultPvP))
  val ophelia       : Opponent = add(Opponent("Ophelia",      defaultPvZ))
  val steamhammer   : Opponent = add(Opponent("Steamhammer",  defaultPvZ))
  val stormbreaker  : Opponent = add(Opponent("Stormbreaker", defaultPvT))
  val xiaoyi        : Opponent = add(Opponent("XiaoYi",       new StrategySelectionRecommended(StrategySelectionGreedy, PvT13Nexus, PvT2BaseCarrier) { duration = 3 }))

  // Returning AIIDE opponents
  val cse           : Opponent = add(Opponent("CSE",          (if (Heartbreak.matches) StrategySelectionFixed(PvP2Gate1012DT) else StrategySelectionFixed(PvP2GateDTExpand))))
  val iron          : Opponent = add(Opponent("Iron",         StrategySelectionFixed(PvT2GateRangeExpandCarrier)))
  val saida         : Opponent = add(Opponent("SAIDA",        StrategySelectionFixed(PvT28Nexus, PvT2BaseCarrier)))
  val adias         : Opponent = add(Opponent("adias",        saida.policy))
  val ualbertabot   : Opponent = add(Opponent("UAlbertaBot",  fixedPvR))
  val zzzkbot       : Opponent = add(Opponent("ZZZKBot",      (if (Benzene.matches)
    StrategySelectionFixed(PvZ4Gate99, PvZMidgame5GateGoon, PvZLateGameTemplar) else
    StrategySelectionFixed(PvZ1BaseForgeTechForced, PvZMidgameNeoBisu, PvZLateGameTemplar)
  )))

  // Other tournaments

  val titaniron     : Opponent = add(Opponent("TitanIron",    new StrategySelectionRecommended(StrategySelectionGreedy, PvT2GateRangeExpandCarrier)))
  val megabot       : Opponent = add(Opponent("MegaBot",      defaultPvP))
  //val aiur          : Opponent = add(Opponent("Aiur",         defaultPvP)) -- Disabled temporarily while testing MetaBot for AIIDE
  val tyr           : Opponent = add(Opponent("Tyr",          StrategySelectionFixed(PvP2GateDTExpand)))
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
  //val jadien            : Opponent = add(Opponent("jadien",             locutus.policy))
  val jadien            : Opponent = add(Opponent("jadien",             StrategySelectionFixed(PvP2Gate1012DT)))
  
  val all: Vector[Opponent] = allKnown
}
