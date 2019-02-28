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
    ))

  val locutusBuilds = StrategySelectionSequence(
    Vector(
      Seq(PvP2Gate1012Goon), // 11-3 -> 13-10 -- it's losing 2-gate mirrors
      Seq(PvP3GateGoonCounter),
      Seq(PvP2GateDTExpand), // 9-7 -> 13-11 -- Weak vs. proxy gate, and drops some games to 4-Gate
      Seq(PvPProxy2Gate) // 5-5 -> 7-5 -- walks into a lot of 2-gate
    ))
  val bananaBuilds = StrategySelectionSequence(
    Vector(
      // 2GateGoon was 9-9
      //Seq(PvPProxy2Gate), // 9-1 -> 4-6 -- lost to 2-Gate but often *beats* 2-Gate
      Seq(PvP2Gate1012Goon), // 13-2 -> 17-6 -- losses were to 2-Gate
      Seq(PvP2GateDTExpand), // 12-3 -> 16-7 -- 2/3 losses were to 2-Gate
      Seq(PvP3GateGoon), //  10-6 -> 19-5 -- rough time vs. 4-Gate in first round; losses were to 2-gate in second round
    ))
  val mcRaveBuilds = StrategySelectionSequence(
    Vector(
      // 2GateGoon was 3-15
      Seq(PvP3GateGoon), // 14-0 -> 17-3 -- 1 loss each to 2-gate, 4-gate, robo
      Seq(PvP2Gate1012Goon), // 12-3 -> 15-4
      Seq(PvP2GateDTExpand), // 12-3 -> 15-5 -- runs into trouble vs 2GateGoon-Robo-4Gate build
      Seq(PvPProxy2Gate), // 10-4 -> 13-6 -- mostly losing to 2-Gate
    ))
  val velocirandomBuilds = StrategySelectionSequence(
    Vector(
      Seq(PvP2GateDTExpand), // 13-6 -> 23-1
      Seq(PvP3GateGoon), // 12-7 -> 23-1
      Seq(PvP2GateGoon), // 22-2
    ))

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
  val jadien            : Opponent = add(Opponent("Jadien",             tscmoo.policy))
  val madmixp           : Opponent = add(Opponent("MadMixP",            madmix.policy))
  val tscmooz           : Opponent = add(Opponent("tscmooz",            tscmoo.policy))
  val martinrooijackers : Opponent = add(Opponent("Martin Rooijackers", letabot.policy))
  val velicorandom      : Opponent = add(Opponent("Velicorandom",       velocirandom.policy))

  val all: Vector[Opponent] = allKnown
}
