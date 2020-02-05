package Lifecycle

import Strategery.{HumanPlaybook, TournamentPlaybook}

object ConfigurationLoader {

  def load(): Unit = {
    val human         = new FileFlag("humanopponent.enabled").apply()
    val ladder        = new FileFlag("ladder.enabled").apply()
    val livestream    = ladder // Until we come up with something better
    val tournament    = new FileFlag("tournament.enabled").apply()
    val roundrobin    = new FileFlag("roundrobin.enabled").apply()
    val elimination   = new FileFlag("elimination.enabled").apply()
    val debugging     = new FileFlag("debugging.enabled").apply()

    if (tournament && ladder) {
      With.logger.warn("Both tournament and ladder modes are enabled")
    }
    if (tournament && livestream) {
      With.logger.warn("Both tournament and livestream modes are enabled")
    }
    if (roundrobin && elimination) {
      With.logger.warn("Both round robin and elimination modes are enabled.")
    }

    if (livestream)     { setLivestreamMode() }
    if (human)          { setHumanMode() }
    if (ladder)         { setLadderMode() }
    if (tournament)     { setTournamentMode() }
    if (roundrobin)     { setRoundRobinMode() }
    if (elimination)    { setEliminationMode() }
    if (debugging)      { setDebugMode() }

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
    config.targetFrameDurationMilliseconds  = 40
    config.forcedPlaybook                   = Some(TournamentPlaybook)
  }

  private def setLadderMode(): Unit = {
    config.targetFrameDurationMilliseconds  = 40
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
    config.visualizeDebug                   = true
    config.enableChat                       = true
    config.enableSurrenders                 = true
    config.targetFrameDurationMilliseconds  = 20
    config.debugging                        = true
    config.debugPauseThreshold              = 24 * 5
  }

  private def config: Configuration = With.configuration
}
