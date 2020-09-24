package Lifecycle

import bwapi.BWClientConfiguration

object Main {

  val configuration = new BWClientConfiguration
  configuration.async = false
  configuration.asyncUnsafe = false
  configuration.autoContinue = true
  configuration.maxFrameDurationMs = 30

  def main(args: Array[String]) {
    JBWAPIClient.startGame(configuration)
  }
}
