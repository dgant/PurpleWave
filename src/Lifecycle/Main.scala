package Lifecycle

import bwapi.BWClientConfiguration

object Main {

  val configuration: BWClientConfiguration = new BWClientConfiguration()
    .withAutoContinue(true)
    .withMaxFrameDurationMs(40)

  // Go async if we have the memory for it
  // and aren't doing live debugging
  private val JBWAPIClientDataSizeBytes = 33017048
  private lazy val memoryFree: Long = Runtime.getRuntime.freeMemory
  private lazy val framesBufferable = Math.min(10, memoryFree / JBWAPIClientDataSizeBytes - 3) // Subtract flat amount as dumb estimate of what the bot needs
  // TODO: Also check that we aren't live-debugging
  if (framesBufferable > 1) {
    //configuration.withAsyncFrameBufferCapacity(framesBufferable.toInt)
    //configuration.withAsync(true)
    //configuration.withAsyncUnsafe(true)
  }

  def main(args: Array[String]) {
    JBWAPIClient.startGame(configuration)
  }
}
