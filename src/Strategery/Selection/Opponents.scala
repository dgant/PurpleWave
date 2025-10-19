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

  val aggroPvZ  : StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ1BaseReactive, PvZSpeedlot, PvZMuscle) { duration = 10 }
  val ecoPvZ    : StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvZFFE) { duration = 10 }

  val defaultPvT: StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), PvTZZCoreZ)
  val defaultPvP: StrategySelectionPolicy = StrategySelectionGreedy()
  val defaultPvZ: StrategySelectionPolicy = aggroPvZ
  val defaultPvR: StrategySelectionPolicy = StrategySelectionGreedy()

  // AIIDE 2025

  // T
  add("InsanityBot",     .9,   defaultPvT)
  add("Void",            .95,  defaultPvT)

  // P
  add("BananaBrain",     .6,   new StrategySelectionRecommended(StrategySelectionGreedy(), PvP1012, PvP5Zealot, PvPDT) { duration = 3 })
  add("Stardust",        .6,  new StrategySelectionRecommended(StrategySelectionGreedy(), PvPGateCore, PvPDT) { duration = 6 })

  // Z
  //add("McRave",          .6,   StrategySelectionFixed(PvZ1BaseReactive, PvZTech, PvZSpeedlot), "McRaveZ")
  add("McRave",          .7,   StrategySelectionGreedy(), "McRaveZ")
  add("Microwave",       .8,   StrategySelectionGreedy())
  add("InfestedArtosis", .9,   aggroPvZ)

  // R
  add("UAlbertaBot",     .95,  StrategySelectionFixed(PvR2Gate4Gate), "Dave Churchill")
  add("C0mputer",        .9,   defaultPvR)
  add("Steamhammer",     .9,   defaultPvR, "Randomhammer")

  val all: Vector[Opponent] = allKnown
}
