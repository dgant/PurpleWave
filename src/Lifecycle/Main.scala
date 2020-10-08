package Lifecycle

import bwapi.BWClientConfiguration

object Main {

  // TODO: These should be selected intelligently.
  // In particular, we should only go async if sufficient memory to support it is available.
  val configuration = new BWClientConfiguration()
    .withAsync(true)
    .withAsyncUnsafe(true)
    .withAutoContinue(true)
    .withMaxFrameDurationMs(30)

  def main(args: Array[String]) {
    JBWAPIClient.startGame(configuration)
  }
}
