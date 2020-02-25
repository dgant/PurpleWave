package Lifecycle

import Mathematics.PurpleMath
import Strategery.Strategies.{AllChoices, Strategy}
import Strategery.{HumanPlaybook, TestingPlaybook, TournamentPlaybook}

object ConfigurationLoader {

  def load(): Unit = {
    val human         = new BooleanFlag("humanopponent")()
    val ladder        = new BooleanFlag("ladder")()
    val livestream    = new BooleanFlag("livestream")() // Until we come up with something better
    val tournament    = new BooleanFlag("tournament")()
    val roundrobin    = new BooleanFlag("roundrobin")()
    val elimination   = new BooleanFlag("elimination")()
    val pretraining   = new BooleanFlag("pretraining")()
    val debugging     = new BooleanFlag("debugging")()
    val debugginglive = new BooleanFlag("debugginglive")()
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
    if (debugging)            { setDebugMode() }
    if (debugginglive)        { setDebugLiveMode() }
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
  }

  private def setTournamentMode(): Unit = {
    config.forcedPlaybook = Some(TournamentPlaybook)
    config.targetFrameDurationMilliseconds = 40
  }

  private def setLadderMode(): Unit = {
    config.targetFrameDurationMilliseconds = 40
  }

  private def setEliminationMode(): Unit = {
    config.recentFingerprints = 2
  }

  private def setRoundRobinMode(): Unit = {
    config.recentFingerprints = 4
  }

  private def setLivestreamMode(): Unit = {
    config.visualizeFun                     = true
    config.enableChat                       = true
    config.enableSurrenders                 = true
    config.targetFrameDurationMilliseconds  = 20
  }

  private def setHumanMode(): Unit = {
    config.humanMode                        = true
    config.strategyRandomness               = 0.4
    config.enableChat                       = true
    config.enableSurrenders                 = true
    config.targetFrameDurationMilliseconds  = 20
    config.forcedPlaybook                   = Some(HumanPlaybook)
  }

  private def setDebugMode(): Unit = {
    config.enableChat                       = true
    config.enableSurrenders                 = true
    config.debugging                        = true
  }

  private def setDebugLiveMode(): Unit = {
    setDebugMode()
    //config.visualizeDebug                   = true
    config.targetFrameDurationMilliseconds  = 20
    config.debugPauseThreshold              = 24 * 5
  }

  private def setFixedBuild(strategyNamesText: String): Unit = {
    val strategyNamesLines = strategyNamesText.split("[\r\n]+").filter(_.nonEmpty).toVector
    val strategyNames = PurpleMath.sample(strategyNamesLines).split(" ").toVector
    val strategyNamesAndInstances = strategyNames.map(name => (name, AllChoices.allVsRandom.find(_.toString.toLowerCase == name.toLowerCase)))
    val strategyNamesUnmatched = strategyNamesAndInstances.filter(_._2.isEmpty).map(_._1)
    val strategyInstances = strategyNamesAndInstances.filter(_._2.nonEmpty).map(_._2.get)
    if (strategyNamesUnmatched.nonEmpty) {
      With.logger.warn("Tried to use fixed build but failed to match builds: (" + strategyNamesUnmatched.mkString(", ") + ") from (" + strategyNamesLines.mkString(", ") + ")")
    }
    if (strategyInstances.nonEmpty) {
      With.logger.debug("Using fixed build: " + strategyNamesText)
      config.forcedPlaybook = Some(new TestingPlaybook {
          override lazy val forced: Seq[Strategy] = strategyInstances
      })
    }
  }

  private def config: Configuration = With.configuration
}
