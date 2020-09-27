package Lifecycle

import bwapi.BWClientConfiguration

object Main {

  val configuration = new BWClientConfiguration
  configuration.async = true
  configuration.asyncUnsafe = true
  configuration.autoContinue = true
  configuration.maxFrameDurationMs = 30

  def main(args: Array[String]) {
    JBWAPIClient.startGame(configuration)
  }
}
