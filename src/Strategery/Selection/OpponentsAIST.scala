package Strategery.Selection

import Strategery.Strategies.Protoss._

object OpponentsAIST {
  private var allKnown: Vector[Opponent] = Vector.empty
  
  def add(opponent: Opponent): Opponent   = {
    allKnown = allKnown :+ opponent
    opponent
  }

  val defaultPvT = StrategySelectionSequence(
    Vector(
      Seq(PvT1015DT, PvT2BaseArbiter),
      Seq(PvT2GateRangeExpand, PvT2BaseCarrier),
      Seq(PvT23Nexus, PvT2BaseArbiter)
    ))

  val defaultPvP = StrategySelectionSequence(
    Vector(
      Seq(PvP2Gate1012Goon),
      Seq(PvP2GateDTExpand),
      Seq(PvP3GateRobo),
      Seq(PvPGateGateRobo),
      Seq(PvP3GateGoon)
    ), loop = true)

  val defaultPvZ = StrategySelectionSequence(
    Vector(
      Seq(PvZFFEConservative, PvZMidgameCorsairReaverGoon,  PvZLateGameReaver),
      Seq(PvZFFEEconomic,     PvZMidgameNeoBisu,            PvZLateGameTemplar),
      Seq(PvZ4Gate99,         PvZMidgame5GateGoonReaver,    PvZLateGameReaver),
      Seq(PvZFFEEconomic,     PvZMidgameBisu,               PvZLateGameTemplar)
    ))

  // Protoss
  val locutus       : Opponent = add(Opponent("Locutus",      defaultPvP))
  val bananabrain   : Opponent = add(Opponent("BananaBrain",  defaultPvP))
  val mcrave        : Opponent = add(Opponent("McRave",       defaultPvP))
  val velocirandom  : Opponent = add(Opponent("Velocirandom", defaultPvP))
  val madmix        : Opponent = add(Opponent("MadMix",       defaultPvP))
  // Terran
  val haopan        : Opponent = add(Opponent("Hao Pan",      defaultPvT))
  val ecgberht      : Opponent = add(Opponent("Ecgberht",     defaultPvT))
  val letabot       : Opponent = add(Opponent("LetaBot",      defaultPvT))
  // Zerg
  val tscmoo        : Opponent = add(Opponent("tscmoo",       defaultPvZ))
  val steamhammer   : Opponent = add(Opponent("Steamhammer",  defaultPvZ))

  // Aliases for local testing
  val jadien            : Opponent = add(Opponent("Jadien",             tscmoo.policy))
  val madmixp           : Opponent = add(Opponent("MadMixP",            madmix.policy))
  val tscmooz           : Opponent = add(Opponent("tscmooz",            tscmoo.policy))
  val martinrooijackers : Opponent = add(Opponent("Martin Rooijackers", letabot.policy))
  val velicorandom      : Opponent = add(Opponent("Velicorandom",       velocirandom.policy))

  
  val all: Vector[Opponent] = allKnown
}
