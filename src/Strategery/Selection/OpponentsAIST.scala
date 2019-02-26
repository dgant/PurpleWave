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
      Seq(PvP2GateGoon),
      Seq(PvP2Gate1012Goon), // 11-3
      Seq(PvP2GateDTExpand), // 9-7 -- was getting hurt vs. 2-Gate (which should be better now)
      Seq(PvP3GateGoon), // 6-10 -- dicey vs. a lot of things
      Seq(PvPProxy2Gate) // 5-5
    ), loop = true) // TODO: Remove
  val bananaBuilds = StrategySelectionSequence(
    Vector(
      Seq(PvP2GateGoon),
      Seq(PvPProxy2Gate), // 9-1 -- lost to 2-Gate but often *beats* 2-Gate
      Seq(PvP2Gate1012Goon), // 13-2 -- losses were to 2-Gate
      Seq(PvP2GateDTExpand), // 12-3 -- 2/3 losses were to 2-Gate
      Seq(PvP3GateGoon), //  10-6 -- rough time vs. 4-Gate
    ), loop = true)  // TODO: Remove
  val mcRaveBuilds = StrategySelectionSequence(
    Vector(
      Seq(PvP2GateGoon),
      Seq(PvP3GateGoon), // 14-0 -- rock solid
      Seq(PvPProxy2Gate), // 10-4 -- mostly losing to 2-Gate
      Seq(PvP2GateDTExpand), // 12-3 -- runs into trouble vs 2GateGoon-Robo-4Gate build
      Seq(PvP2Gate1012Goon), // 12-3
    ), loop = true)  // TODO: Remove
  val velocirandomBuilds = StrategySelectionSequence(
    Vector(
      Seq(PvP2GateGoon),
      Seq(PvP3GateGoon), // 12-7 -- probably much stronger now after ramp+build fix
      Seq(PvP2Gate1012Goon), // 5-15 -- maybe better after ramp fix
      Seq(PvP2GateDTExpand), // 13-6 -- probably much stronger now after ramp+build fix
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
