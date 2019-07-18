package Strategery.Selection

import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  
  def add(opponent: Opponent): Opponent   = {
    allKnown = allKnown :+ opponent
    opponent
  }

  val defaultPvT  = new StrategySelectionRecommended(StrategySelectionGreedy, PvT1015DT, PvT3BaseCarrier)
  val defaultPvP  = new StrategySelectionRecommended(StrategySelectionGreedy, PvP2Gate1012DT)
  val defaultPvZ  = new StrategySelectionRecommended(StrategySelectionGreedy, PvZ4Gate99, PvZMidgame5GateGoon, PvZLateGameTemplar)
  val fixedPvT    = new StrategySelectionFixed(PvT1015DT, PvT3BaseArbiter)
  val fixedPvZ    = new StrategySelectionFixed(PvZ4Gate99, PvZMidgame5GateGoon, PvZLateGameTemplar)
  val fixedPvR    = new StrategySelectionFixed(PvR1BaseDT)
  
  val mcrave        : Opponent = add(Opponent("McRave",       StrategySelectionDynamic))
  val locutus       : Opponent = add(Opponent("Locutus",      StrategySelectionSequence(Vector(Seq(PvP2GateGoon), Seq(PvP2Gate1012DT), Seq(PvP3GateGoonCounter), Seq(PvP2GateDTExpand), Seq(PvPProxy2Gate)))))
  val tscmoo        : Opponent = add(Opponent("tscmoo",       StrategySelectionSequence(Seq(
    Seq(
      PvRProxy2Gate,
      PvT2GateRangeExpandCarrier,
      PvP4GateGoon,
      PvZ4Gate99, PvZMidgame5GateGoon, PvZLateGameTemplar),
    Seq(
      PvROpenZCoreZ,
      PvT21Nexus, PvT2BaseCarrier,
      PvP3GateGoon,
      PvZ4Gate1012, PvZMidgameNeoBisu, PvZLateGameTemplar),
    Seq(PvR2Gate4Gate)
  ))))
  val bananabrain   : Opponent = add(Opponent("BananaBrain",  new StrategySelectionRecommended(StrategySelectionGreedy, PvP2GateDTExpand) { duration = 20 }))
  val iron          : Opponent = add(Opponent("Iron",         new StrategySelectionFixed(PvT2GateRangeExpandCarrier)))
  val titaniron     : Opponent = add(Opponent("TitanIron",    new StrategySelectionRecommended(StrategySelectionGreedy, PvT2GateRangeExpandCarrier)))
  val letabot       : Opponent = add(Opponent("LetaBot",      new StrategySelectionRecommended(StrategySelectionGreedy, PvTReaverCarrierCheese)))
  val steamhammer   : Opponent = add(Opponent("Steamhammer",  StrategySelectionDynamic))
  val microwave     : Opponent = add(Opponent("Microwave",    new StrategySelectionRecommended(StrategySelectionDynamic, PvZ4Gate1012, PvZMidgame5GateGoon, PvZLateGameTemplar)))
  val megabot       : Opponent = add(Opponent("MegaBot",      defaultPvP))
  val zzzkbot       : Opponent = add(Opponent("ZZZKBot",      new StrategySelectionFixed(PvZ1BaseForgeTech, PvZMidgameNeoBisu, PvZLateGameTemplar)))
  val ualbertabot   : Opponent = add(Opponent("UAlbertaBot",  fixedPvR))
  val aiur          : Opponent = add(Opponent("Aiur",         defaultPvP))
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
  val stormbreaker  : Opponent = add(Opponent("Stormbreaker", defaultPvZ))

  // Aliases
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
