package Lifecycle

import bwapi.BWClientConfiguration

object Main {

  val configuration = new BWClientConfiguration
  configuration.async = true
  configuration.autoContinue = false
  configuration.maxFrameDurationMs = 10
  configuration.asyncUnsafe = true

  def main(args: Array[String]) {
    JBWAPIClient.startGame(configuration)
  }
}
