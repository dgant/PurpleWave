package Lifecycle

import bwapi.BWClientConfiguration

object Main {

  def main(args: Array[String]) {
    val configuration = new BWClientConfiguration
    //configuration.async = true
    configuration.autoContinue = true
    JBWAPIClient.startGame(configuration)
  }
}
