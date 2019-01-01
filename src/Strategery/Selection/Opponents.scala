package Strategery.Selection

import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  
  def add(opponent: Opponent): Opponent   = {
    allKnown = allKnown :+ opponent
    opponent
  }

  val defaultPvT  = new StrategySelectionRecommended(StrategySelectionGreedy, PvT1015DT, PvT3BaseCarrier)
  val defaultPvP  = new StrategySelectionRecommended(StrategySelectionGreedy, PvP2Gate1012, PvPLateGameArbiter)
  val defaultPvZ  = new StrategySelectionRecommended(StrategySelectionGreedy, PvZ4Gate99, PvZMidgame5GateGoon)
  val fixedPvT    = new StrategySelectionFixed(PvT1015DT, PvT3BaseArbiter)
  val fixedPvZ    = new StrategySelectionFixed(PvZ4Gate99, PvZMidgame5GateGoon)
  val fixedPvR    = new StrategySelectionFixed(PvROpenTinfoil)
  
  val mcrave        : Opponent = add(Opponent("McRave",       StrategySelectionDynamic))
  val titaniron     : Opponent = add(Opponent("TitanIron",    new StrategySelectionRecommended(StrategySelectionDynamic, PvT1015DT, PvT2BaseCarrier)))
  val locutus       : Opponent = add(Opponent("Locutus",      new StrategySelectionRecommended(StrategySelectionDynamic, PvPProxy2Gate, PvPLateGameArbiter)))
  val tscmoo        : Opponent = add(Opponent("tscmoo",       StrategySelectionGreedy))
  val iron          : Opponent = add(Opponent("Iron",         new StrategySelectionFixed(PvT1015DT, PvT2BaseCarrier)))
  val letabot       : Opponent = add(Opponent("LetaBot",      new StrategySelectionRecommended(StrategySelectionGreedy, PvTReaverCarrierCheese)))
  val steamhammer   : Opponent = add(Opponent("Steamhammer",  new StrategySelectionRecommended(StrategySelectionDynamic, PvZ4Gate99, PvZMidgame5GateGoon)))
  val microwave     : Opponent = add(Opponent("Microwave",    new StrategySelectionRecommended(StrategySelectionDynamic, PvZ4Gate99, PvZMidgame5GateGoon)))
  val megabot       : Opponent = add(Opponent("MegaBot",      defaultPvP))
  val zzzkbot       : Opponent = add(Opponent("ZZZKBot",      fixedPvZ))
  val ualbertabot   : Opponent = add(Opponent("UAlbertaBot",  fixedPvR))
  val aiur          : Opponent = add(Opponent("Aiur",         defaultPvP))
  val tyr           : Opponent = add(Opponent("Tyr",          new StrategySelectionRecommended(StrategySelectionGreedy, PvP2GateDTExpand, PvPLateGameArbiter)))
  val ecgberht      : Opponent = add(Opponent("Ecgberht",     defaultPvT))
  val overkill      : Opponent = add(Opponent("Overkill",     fixedPvZ))
  val ziabot        : Opponent = add(Opponent("Ziabot",       fixedPvZ))
  val opprimobot    : Opponent = add(Opponent("OpprimoBot",   fixedPvR))
  val bonjwa        : Opponent = add(Opponent("Bonjwa",       fixedPvT))
  val terranuab     : Opponent = add(Opponent("TerranUAB",    fixedPvT))
  val srbotone      : Opponent = add(Opponent("SRbotOne",     fixedPvT))
  val sling         : Opponent = add(Opponent("Sling",        fixedPvZ))
  val salsa         : Opponent = add(Opponent("Salsa",        fixedPvZ))
  val bonjwai       : Opponent = add(Opponent("bonjwAI",      defaultPvT))
  val korean        : Opponent = add(Opponent("Korean",       defaultPvZ))
  val cunybot       : Opponent = add(Opponent("CUNYBot",      defaultPvZ))
  val hellbot       : Opponent = add(Opponent("Hellbot",      defaultPvP))
  val isamind       : Opponent = add(Opponent("ISAMind",      defaultPvP))
  val tttkbot       : Opponent = add(Opponent("TTTKBot",      defaultPvT))
  val stormbreaker  : Opponent = add(Opponent("Stormbreaker", defaultPvZ))

  // Aliases for local testing
  val tscmoor           : Opponent = add(Opponent("tscmoor",            tscmoo.policy))
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
  
  val all: Vector[Opponent] = allKnown
}
