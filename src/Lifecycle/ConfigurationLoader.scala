package Lifecycle

import java.io.File

import Mathematics.PurpleMath
import Strategery.Selection.{ExpandStrategy, StrategySelectionGreedy, StrategySelectionPolicy}
import Strategery.Strategies.{AllChoices, Strategy}
import Strategery.{HumanPlaybook, PretrainingPlaybook, TestingPlaybook, TournamentPlaybook}
import mjson.Json

object ConfigurationLoader {

  def getOrDefault[T](json: Json, property: String, default: T): T = {
    val value = json.at(property)
    if (value == null) {
      return default
    }
    val attemptedOutput: T = default match {
      case _: Boolean =>  if (value.isBoolean)  value.asBoolean().asInstanceOf[T] else default
      case _: Int =>      if (value.isNumber)   value.asInteger().asInstanceOf[T] else default
      case _: Double =>   if (value.isNumber)   value.asDouble().asInstanceOf[T] else default
      case _ =>           if (value.isString)   value.asString().asInstanceOf[T] else default
    }
    if (attemptedOutput == null) {
      return default
    }
    attemptedOutput
  }

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
      val configText    = scala.io.Source.fromFile(configFile).mkString
      val config        = Json.read(configText)
      val human         = getOrDefault(config, "human",         false)
      val ladder        = getOrDefault(config, "ladder",        false)
      val livestream    = getOrDefault(config, "livestream",    false)
      val tournament    = getOrDefault(config, "tournament",    false)
      val roundrobin    = getOrDefault(config, "roundrobin",    false)
      val elimination   = getOrDefault(config, "elimination",   false)
      val pretraining   = getOrDefault(config, "pretraining",   false)
      val debugging     = getOrDefault(config, "debugging",     false)
      val debugginglive = getOrDefault(config, "debugginglive", false)
      val logstd        = getOrDefault(config, "logstd",        false)
      val frameMsTarget = getOrDefault(config, "framemstarget", With.configuration.frameTargetMs)
      val frameMsLimit  = getOrDefault(config, "framemslimit",  With.configuration.frameLimitMs)
      val fixedbuilds   = new FileFlag("fixedbuilds.txt").contents

      With.configuration.frameTargetMs = frameMsTarget
      With.configuration.frameLimitMs = frameMsLimit

      if (tournament    && ladder)      With.logger.warn("Both tournament and ladder modes are enabled")
      if (tournament    && livestream)  With.logger.warn("Both tournament and livestream modes are enabled")
      if (tournament    && pretraining) With.logger.warn("Both tournament and pretraining modes are enabled")
      if (pretraining   && ladder)      With.logger.warn("Both pretraining and laddermodes are enabled")
      if (pretraining   && livestream)  With.logger.warn("Both pretraining and livestream modes are enabled")
      if (roundrobin    && elimination) With.logger.warn("Both round robin and elimination modes are enabled")
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
      if (logstd)               { With.configuration.logstd = true }
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
    } catch { case exception: Exception => With.logger.onException(exception) }
  }

  private def setTournamentMode(): Unit = {
    config.forcedPlaybook = Some(TournamentPlaybook)
  }

  private def setLadderMode(): Unit = {
    config.historyHalfLife    = 40
    config.recentFingerprints = 4
  }

  private def setEliminationMode(): Unit = {
    config.recentFingerprints = 2
  }

  private def setRoundRobinMode(): Unit = {
    config.historyHalfLife    = 40
    config.recentFingerprints = 4
  }

  private def setLivestreamMode(): Unit = {
    config.visualizeFun       = true
    config.enableChat         = true
    config.enableSurrenders   = true
  }

  private def setHumanMode(): Unit = {
    config.humanMode          = true
    config.enableChat         = true
    config.enableSurrenders   = true
    config.forcedPlaybook     = Some(HumanPlaybook)
  }

  private def setPretraining(): Unit = {
    config.forcedPlaybook     = Some(PretrainingPlaybook)
  }

  private def setDebugMode(): Unit = {
    config.enableChat         = true
    config.enableSurrenders   = true
    config.debugging          = true
  }

  private def setDebugLiveMode(): Unit = {
    setDebugMode()
    config.visualizeDebug     = true
    config.detectBreakpoints  = true
    config.forcedPlaybook     = Some(new TestingPlaybook)
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
