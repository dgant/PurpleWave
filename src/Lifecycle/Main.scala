package Lifecycle

import bwapi.BWClientConfiguration

object Main {

  val configuration = new BWClientConfiguration
  configuration.async = false
  configuration.autoContinue = true

  def main(args: Array[String]) {
    JBWAPIClient.startGame(configuration)
  }
}
