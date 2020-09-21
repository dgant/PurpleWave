package Lifecycle

import bwapi.BWClientConfiguration

object Main {

  val configuration = new BWClientConfiguration
  configuration.async = true
  configuration.autoContinue = true
  configuration.maxFrameDurationMs = 20
  configuration.asyncUnsafe = true

  def main(args: Array[String]) {
    JBWAPIClient.startGame(configuration)
  }
}
