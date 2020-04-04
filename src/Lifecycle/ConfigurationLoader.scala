package Lifecycle

import java.io.File

import Mathematics.PurpleMath
import Strategery.Selection.{ExpandStrategy, StrategySelectionGreedy, StrategySelectionPolicy}
import Strategery.Strategies.{AllChoices, Strategy}
import Strategery.{HumanPlaybook, PretrainingPlaybook, TestingPlaybook, TournamentPlaybook}

object ConfigurationLoader {

  def load(): Unit = {
    val configFiles: Array[File] = new File(With.bwapiData.ai).listFiles.filter(f => Seq("Purple", ".config", ".json").forall(f.getName.contains))
    val configFilesMatching = Seq(configFiles.filter(_.getName.contains(With.self.name)), configFiles).find(_.nonEmpty).getOrElse(Array.empty)
    if (configFilesMatching.isEmpty) {
      With.logger.warn("Didn't find any matching configurations")
      return
    }
    val configFile = configFiles.head
    if (configFilesMatching.length > 1) {
      With.logger.warn("Found multiple matching configurations: " + configFilesMatching.map(_.getName).mkString(", "))
    }
    With.logger.debug("Using configuration file " + configFile.getName)
    try {
      /*
      val configText    = scala.io.Source.fromFile(configFile).mkString
      val config        = play.api.libs.json.Json.parse(configText)

      val human         = (config \ "human")          .asOpt[Boolean].getOrElse(false)
      val ladder        = (config \ "ladder")         .asOpt[Boolean].getOrElse(false)
      val livestream    = (config \ "livestream")     .asOpt[Boolean].getOrElse(false)
      val tournament    = (config \ "tournament")     .asOpt[Boolean].getOrElse(false)
      val roundrobin    = (config \ "roundrobin")     .asOpt[Boolean].getOrElse(false)
      val elimination   = (config \ "elimination")    .asOpt[Boolean].getOrElse(false)
      val pretraining   = (config \ "pretraining")    .asOpt[Boolean].getOrElse(false)
      val debugging     = (config \ "debugging")      .asOpt[Boolean].getOrElse(false)
      val debugginglive = (config \ "debugginglive")  .asOpt[Boolean].getOrElse(false)
      val logstd        = (config \ "logstd")         .asOpt[Boolean].getOrElse(false)
      val fixedbuilds   = new FileFlag("fixedbuilds.txt").contents

      if (tournament  && ladder)        With.logger.warn("Both tournament and ladder modes are enabled")
      if (tournament  && livestream)    With.logger.warn("Both tournament and livestream modes are enabled")
      if (tournament  && pretraining)   With.logger.warn("Both tournament and pretraining modes are enabled")
      if (pretraining && ladder)        With.logger.warn("Both pretraining and laddermodes are enabled")
      if (pretraining && livestream)    With.logger.warn("Both pretraining and livestream modes are enabled")
      if (roundrobin  && elimination)   With.logger.warn("Both round robin and elimination modes are enabled")
      if (debugginglive && ! debugging) With.logger.warn("debugginglive enabled without debugging enabled")

      if (livestream)           { setLivestreamMode() }
      if (human)                { setHumanMode() }
      if (ladder)               { setLadderMode() }
      if (tournament)           { setTournamentMode() }
      if (roundrobin)           { setRoundRobinMode() }
      if (elimination)          { setEliminationMode() }
      if (pretraining)          { setPretraining() }
      if (debugging)            { setDebugMode() }
      if (debugginglive)        { setDebugLiveMode() }
      if (logstd)               { With.configuration.logstd }
      if (fixedbuilds.nonEmpty) { setFixedBuild(fixedbuilds) }

      Seq(
        ("Human", human.toString),
        ("Ladder", ladder.toString),
        ("Stream", livestream),
        ("Tournament", tournament.toString),
        ("Round-robin", roundrobin.toString),
        ("Elimination", elimination.toString),
        ("Debugging", debugging.toString))
      .foreach(pair => With.logger.debug(pair._1 + ": " + pair._2))
      */
    } catch { case exception: Exception => With.logger.onException(exception) }
  }

  private def setTournamentMode(): Unit = {
    config.forcedPlaybook = Some(TournamentPlaybook)
    config.targetFrameDurationMilliseconds = 40
  }

  private def setLadderMode(): Unit = {
    config.historyHalfLife                  = 40
    config.targetFrameDurationMilliseconds  = 40
    config.recentFingerprints               = 4
  }

  private def setEliminationMode(): Unit = {
    config.recentFingerprints               = 2
  }

  private def setRoundRobinMode(): Unit = {
    config.historyHalfLife                  = 40
    config.recentFingerprints               = 4
  }

  private def setLivestreamMode(): Unit = {
    config.visualizeFun                     = true
    config.enableChat                       = true
    config.enableSurrenders                 = true
    config.targetFrameDurationMilliseconds  = 20
  }

  private def setHumanMode(): Unit = {
    config.humanMode                        = true
    config.enableChat                       = true
    config.enableSurrenders                 = true
    config.targetFrameDurationMilliseconds  = 20
    config.forcedPlaybook                   = Some(HumanPlaybook)
  }

  private def setPretraining(): Unit = {
    config.historyHalfLife                  = 20
    config.forcedPlaybook                   = Some(PretrainingPlaybook)
  }

  private def setDebugMode(): Unit = {
    config.enableChat                       = true
    config.enableSurrenders                 = true
    config.debugging                        = true
  }

  private def setDebugLiveMode(): Unit = {
    setDebugMode()
    config.visualizeDebug                   = true
    config.targetFrameDurationMilliseconds  = 20
    config.debugPauseThreshold              = 24 * 5
    config.forcedPlaybook                   = Some(new TestingPlaybook)
  }

  def matchNames(names: Seq[String], branches: Seq[Seq[Strategy]]): Seq[Seq[Strategy]] = {
    branches.filter(branch => names.forall(name => branch.exists(_.toString == name)))
  }

  private def setFixedBuild(strategyNamesText: String): Unit = {
    // The implementation of this is a little tricky because we have to call this before the Strategist has been instantiated

    // Get all the strategy names
    val strategyNamesLines = strategyNamesText.replaceAll(",", " ").replaceAll("  ", " ").split("[\r\n]+").filter(_.nonEmpty).toVector
    val strategyNames = PurpleMath.sample(strategyNamesLines).split(" ")

    // Get all the mapped strategy objects
    var matchingBranches = matchNames(strategyNames, AllChoices.tree.flatMap(ExpandStrategy.apply).distinct)
    if (matchingBranches.isEmpty) {
      With.logger.warn("Tried to use fixed build but failed to match " + strategyNamesText)
    }
    if (matchingBranches.nonEmpty) {
      With.logger.debug("Using fixed build: " + strategyNamesText)
      config.forcedPlaybook = Some(new TestingPlaybook {
        override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionGreedy(Some(matchingBranches))
      })
    }
  }

  private def config: Configuration = With.configuration
}
