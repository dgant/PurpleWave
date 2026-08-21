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

  val defaultPvT: StrategySelectionPolicy = StrategySelectionGreedy()
  val defaultPvP: StrategySelectionPolicy = StrategySelectionGreedy()
  val defaultPvZ: StrategySelectionPolicy = StrategySelectionGreedy()
  val defaultPvR: StrategySelectionPolicy = StrategySelectionGreedy()

  // COG 2026

  // P
  add("BananaBrain",     .55, new StrategySelectionRecommended(StrategySelectionGreedy(), 8,  PvPGateCore, PvPDT))
  add("Stardust",        .55, new StrategySelectionRecommended(StrategySelectionGreedy(), 4,  PvPGateCore, PvPDT))
  add("Venator",         .9,  new StrategySelectionRecommended(StrategySelectionGreedy(), 10, PvPGateCore, PvPRobo))

  // Z
  //add("McRave",          .6,   StrategySelectionFixed(PvZ1BaseReactive, PvZTech, PvZSpeedlot), "McRaveZ")
  //add("McRave",          .7,   StrategySelectionGreedy(), "McRaveZ")
  add("McRave",           .6,   new StrategySelectionRecommended(StrategySelectionGreedy(), 10, PvZ1GateCoreYoloSpeedlot), "McRaveZ")
  add("Microwave",        .7,   defaultPvZ)
  add("InfestedArtosis",  .8,   defaultPvZ)
  add("WATTBOT",          .8,   defaultPvZ)

  // R
  add("Pluto",           .1,   defaultPvR)
  add("Steamhammer",     .8,   defaultPvR, "Randomhammer")
  add("UAlbertaBot",     .9,   StrategySelectionFixed(PvR2Gate4Gate), "Dave Churchill")

  val all: Vector[Opponent] = allKnown
}
