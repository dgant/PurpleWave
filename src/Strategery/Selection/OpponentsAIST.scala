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
      Seq(PvP3GateGoon),
      Seq(PvPProxy2Gate)))

  val defaultPvZ = StrategySelectionSequence(
    Vector(
      Seq(PvZFFEConservative, PvZMidgameCorsairReaverGoon,  PvZLateGameReaver),
      Seq(PvZFFEEconomic,     PvZMidgameNeoBisu,            PvZLateGameTemplar),
      Seq(PvZ4Gate99,         PvZMidgame5GateGoonReaver,    PvZLateGameReaver),
      Seq(PvZFFEEconomic,     PvZMidgameBisu,               PvZLateGameTemplar)
    ))

  val locutusBuilds = StrategySelectionSequence(
    Vector(
      Seq(PvP2Gate1012Goon), // Doing well but was suffering against gas steal and getting confused by fake DT
      Seq(PvP2GateDTExpand), // Best build
      Seq(PvP3GateGoon), // Dicey vs. 4-Gate
      Seq(PvPProxy2Gate) // Low data
    ), loop = true) // TODO: Remove
  val bananaBuilds = StrategySelectionSequence(
    Vector(
      Seq(PvP2Gate1012Goon), // Best build
      Seq(PvP2GateDTExpand), // Pretty good but walks into Robo builds sometimes
      Seq(PvP3GateGoon), //  No data
      Seq(PvPProxy2Gate) // Speculative try; no data
    ), loop = true)  // TODO: Remove
  val mcRaveBuilds = StrategySelectionSequence(
    Vector(
      Seq(PvP2Gate1012Goon), // Works okay; forces 2-Gate reaction; sometimes dies to DT counter
      Seq(PvPProxy2Gate), // No data
      Seq(PvP2GateDTExpand), // Best build
      Seq(PvP3GateGoon) // No info
    ), loop = true)  // TODO: Remove
  val velocirandomBuilds = StrategySelectionSequence(
    Vector(
      Seq(PvP2Gate1012Goon), // Surprisingly weak
      Seq(PvP2GateDTExpand), // Best build
      Seq(PvP3GateGoon) // Pretty good but can flake
    ), loop = true)  // TODO: Remove

  val anything = StrategySelectionGreedy

  // Protoss
  val locutus       : Opponent = add(Opponent("Locutus",      locutusBuilds))
  val bananabrain   : Opponent = add(Opponent("BananaBrain",  bananaBuilds))
  val mcrave        : Opponent = add(Opponent("McRave",       mcRaveBuilds))
  val velocirandom  : Opponent = add(Opponent("Velocirandom", velocirandomBuilds))
  val madmix        : Opponent = add(Opponent("MadMix",       defaultPvP))
  // Terran
  val haopan        : Opponent = add(Opponent("Hao Pan",      defaultPvT))
  val ecgberht      : Opponent = add(Opponent("Ecgberht",     defaultPvT))
  val letabot       : Opponent = add(Opponent("LetaBot",      defaultPvT))
  // Zerg
  val tscmoo        : Opponent = add(Opponent("tscmoo",       defaultPvZ))
  val steamhammer   : Opponent = add(Opponent("Steamhammer",  defaultPvZ))

  // Aliases for local testing
  val jadien            : Opponent = add(Opponent("Jadien",             velocirandomBuilds))
  val madmixp           : Opponent = add(Opponent("MadMixP",            madmix.policy))
  val tscmooz           : Opponent = add(Opponent("tscmooz",            tscmoo.policy))
  val martinrooijackers : Opponent = add(Opponent("Martin Rooijackers", letabot.policy))
  val velicorandom      : Opponent = add(Opponent("Velicorandom",       velocirandom.policy))

  val all: Vector[Opponent] = allKnown
}
