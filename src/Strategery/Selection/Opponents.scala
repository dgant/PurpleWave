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

  val defaultPvT: StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), 5, 0.75, PvT1BaseReaver, PvTDoubleRobo)
  val defaultPvP: StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), 5, 0.75, PvPGateCore, PvPDT)
  val defaultPvZ: StrategySelectionPolicy = new StrategySelectionRecommended(StrategySelectionGreedy(), 5, 0.75, PvZFFE)
  val defaultPvR: StrategySelectionPolicy = StrategySelectionFixed(PvR2Gate4Gate)

  // COG 2026

  /*
  // P
  add("BananaBrain",     .65, new StrategySelectionRecommended(StrategySelectionGreedy(), 10, PvPGateCore, PvPDT))
  add("Stardust",        .65, new StrategySelectionRecommended(StrategySelectionGreedy(), 10, PvPGateCore, PvPDT))
  add("Venator",         .9,  new StrategySelectionRecommended(StrategySelectionGreedy(), 10, PvPGateCore, PvPRobo))

  // Z
  add("McRave",           .65,  new StrategySelectionRecommended(StrategySelectionGreedy(), 10, PvZ1GateCoreYoloSpeedlot), "McRaveZ")
  add("Microwave",        .7,   defaultPvZ)
  add("InfestedArtosis",  .8,   defaultPvZ)
  add("WATTBOT",          .8,   defaultPvZ)

  // R
  add("Pluto",           .55,  defaultPvR)
  add("Steamhammer",     .8,   defaultPvR, "Randomhammer")
  add("UAlbertaBot",     .9,   StrategySelectionFixed(PvR2Gate4Gate), "Dave Churchill")
*/
  val all: Vector[Opponent] = allKnown
}
