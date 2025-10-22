package Strategery.Selection

import Strategery.Strategies.Protoss._

object Opponents {

  private var allKnown: Vector[Opponent] = Vector.empty

  private def add(opponent: Opponent): Unit = allKnown = {
    allKnown :+ opponent
  }
  private def add(name: String, targetWinrate: Double, policy: StrategySelectionPolicy, aliases: String*): Unit = {
    val opponent = Opponent(name, targetWinrate, policy)
    (aliases :+ name).foreach(add(_,opponent))
  }
  private def add(name: String, other: Opponent): Unit = {
    add(new Opponent(name, other))
  }

  val ecoPvZ    : StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvZFFE) { duration = 10 }

  val defaultPvT: StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvTZZCoreZ, PvTDoubleRobo)
  val defaultPvP: StrategySelectionPolicy = StrategySelectionGreedy()
  val defaultPvZ: StrategySelectionPolicy = StrategySelectionGreedy()
  val defaultPvR: StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvR2Gate4Gate)

  // AIIDE 2025

  // T
  add("InsanityBot",     .9,   defaultPvT)
  add("Void",            .95,  defaultPvT)

  // P
  add("BananaBrain",     .55, new StrategySelectionRecommended(StrategySelectionGreedy()))
  add("Stardust",        .55, new StrategySelectionRecommended(StrategySelectionGreedy(), PvPGateCore, PvPDT) { duration = 10 })

  // Z
  //add("McRave",          .6,   StrategySelectionFixed(PvZ1BaseReactive, PvZTech, PvZSpeedlot), "McRaveZ")
  //add("McRave",          .7,   StrategySelectionGreedy(), "McRaveZ")
  add("McRave",          .7,   defaultPvZ, "McRaveZ", "McRaveZB", "McRaveZC")
  add("Microwave",       .7,   defaultPvZ)
  add("InfestedArtosis", .9,   defaultPvZ)

  // R
  add("UAlbertaBot",     .9,   StrategySelectionFixed(PvR2Gate4Gate), "Dave Churchill")
  add("C0mputer",        .9,   defaultPvR)
  add("Steamhammer",     .9,   defaultPvR, "Randomhammer")

  val all: Vector[Opponent] = allKnown
}
