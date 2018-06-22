package Strategery.Selection

import Strategery.Strategies.Protoss.{PvTEarly1015GateGoonDT, PvZ4Gate99}

case class Opponent(name: String, policy: StrategySelectionPolicy = StrategySelectionGreedy) {
  def matches(otherName: String): Boolean = {
    name == otherName
  }
  def matchesLoosely(otherName: String): Boolean = {
    val n1 = name.toLowerCase
    val n2 = otherName.toLowerCase
    n1 == n2
  }
  def matchesVeryLoosely(otherName: String): Boolean = {
    val n1 = name.toLowerCase
    val n2 = otherName.toLowerCase
    n1.contains(n2) || n2.contains(n1)
  }
}

object Opponents {
  private var allInAssembly: Vector[Opponent] = Vector.empty
  
  def add(opponent: Opponent): Opponent   = {
    allInAssembly = allInAssembly :+ opponent
    opponent
  }
  
  val mcrave        : Opponent = add(Opponent("McRave",       StrategySelectionDynamic))
  val titaniron     : Opponent = add(Opponent("TitanIron",    StrategySelectionDynamic))
  val locutus       : Opponent = add(Opponent("Locutus",      StrategySelectionDynamic))
  val tscmoo        : Opponent = add(Opponent("tscmoo",       StrategySelectionGreedy))
  val iron          : Opponent = add(Opponent("Iron",         StrategySelectionFixed(PvTEarly1015GateGoonDT)))
  val letabot       : Opponent = add(Opponent("LetaBot",      StrategySelectionGreedy))
  val steamhammer   : Opponent = add(Opponent("Steamhammer",  StrategySelectionDynamic))
  val microwave     : Opponent = add(Opponent("Microwave",    StrategySelectionDynamic))
  val megabot       : Opponent = add(Opponent("MegaBot"))     // TODO: Fixed
  val zzzkbot       : Opponent = add(Opponent("ZZZKBot",     StrategySelectionFixed(PvZ4Gate99)))
  val ualbertabot   : Opponent = add(Opponent("UAlbertaBot")) // TODO: Fixed
  val aiur          : Opponent = add(Opponent("Aiur"))        // TODO: Fixed
  val tyr           : Opponent = add(Opponent("Tyr",          StrategySelectionGreedy))
  val ecgberht      : Opponent = add(Opponent("Ecgberht",     StrategySelectionGreedy))
  val overkill      : Opponent = add(Opponent("Overkill",     StrategySelectionFixed(PvZ4Gate99)))
  val ziabot        : Opponent = add(Opponent("Ziabot",       StrategySelectionFixed(PvZ4Gate99)))
  val opprimobot    : Opponent = add(Opponent("OpprimoBot"))  // TODO: Fixed
  val bonjwa        : Opponent = add(Opponent("Bonjwa",       StrategySelectionFixed(PvTEarly1015GateGoonDT)))
  val terranuab     : Opponent = add(Opponent("TerranUAB",    StrategySelectionFixed(PvTEarly1015GateGoonDT)))
  val srbotone      : Opponent = add(Opponent("SRbotOne",     StrategySelectionFixed(PvTEarly1015GateGoonDT)))
  val sling         : Opponent = add(Opponent("Sling",        StrategySelectionFixed(PvZ4Gate99)))
  val salsa         : Opponent = add(Opponent("Salsa",        StrategySelectionFixed(PvZ4Gate99)))
  val bonjwai       : Opponent = add(Opponent("bonjwAI",      StrategySelectionGreedy))
  val korean        : Opponent = add(Opponent("Korean",       StrategySelectionGreedy))
  val cunybot       : Opponent = add(Opponent("CUNYBot",      StrategySelectionGreedy))
  val hellbot       : Opponent = add(Opponent("Hellbot",      StrategySelectionGreedy))
  val isamind       : Opponent = add(Opponent("ISAMind",      StrategySelectionGreedy))
  val tttkbot       : Opponent = add(Opponent("TTTKBot",      StrategySelectionGreedy))
  val stormbreaker  : Opponent = add(Opponent("Stormbreaker", StrategySelectionGreedy))
  
  val all: Vector[Opponent] = allInAssembly
}
