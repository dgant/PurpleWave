package Strategery.Selection

import Strategery.Strategies.Protoss._

object Opponents {
  private var allKnown: Vector[Opponent] = Vector.empty
  private def add(name: String, policy: StrategySelectionPolicy = StrategySelectionGreedy()): Opponent = { val output = Opponent(name, policy); allKnown = allKnown :+ output; output }

  // COG 2020
  val microwave   : Opponent = add("Microwave")
  val bananabrain : Opponent = add("BananaBrain")
  val zzzkbot     : Opponent = add("ZZZKBot",   new StrategySelectionRecommended(StrategySelectionGreedy(), PvZ1BaseForgeTech, PvZMidgameNeoBisu, PvZLateGameTemplar))
  val metabot     : Opponent = add("MetaBot",   StrategySelectionFixed(PvPRobo))
  val betastar    : Opponent = add("BetaStar")
  val xiaoyi      : Opponent = add("XIAOYI",    StrategySelectionFixed(PvT13Nexus, PvT2BaseCarrier))

  // Aliased
  val iron        : Opponent = add("Iron")
  val letabot     : Opponent = add("LetaBot")
  val megabot     : Opponent = add("MegaBot")
  //val zzzkbot     : Opponent = add("ZZZKBot")
  val ualbertabot : Opponent = add("UAlbertaBot")
  val aiur        : Opponent = add("AIUR")
  val overkill    : Opponent = add("Overkill")
  val zia         : Opponent = add("Zia")
  val srbotone    : Opponent = add("SRBotOne")
  val cunybot     : Opponent = add("CUNYBot")

  // Aliases
  val ironbot           : Opponent = add("Iron bot",           iron.policy)
  val martinrooijackers : Opponent = add("Martin Rooijackers", letabot.policy)
  val megabot2017       : Opponent = add("MegaBot2017",        megabot.policy)
  val chriscoxe         : Opponent = add("Chris Coxe",         zzzkbot.policy)
  val davechurchill     : Opponent = add("Dave Churchill",     ualbertabot.policy)
  val florianrichoux    : Opponent = add("Florian Richoux",    aiur.policy)
  val sijiaxu           : Opponent = add("Sijia Xu",           overkill.policy)
  val ziabot            : Opponent = add("Zia bot",            zia.policy)
  val johankayser       : Opponent = add("Johan Kayser",       srbotone.policy)
  val bryanweber        : Opponent = add("Bryan Weber",        cunybot.policy)
  val jadien            : Opponent = add("jadien",             iron.policy)
  
  val all: Vector[Opponent] = allKnown
}
