package Lifecycle

import java.lang.management.ManagementFactory

import bwapi.BWClientConfiguration

import scala.collection.JavaConverters._

object Main {
  private val JBWAPIClientDataSizeBytes =  33017048
  private val botRequiredDataSizeBytes  = 500000000

  private val memoryFree  : Long            = Math.min(Runtime.getRuntime.maxMemory, Runtime.getRuntime.totalMemory)
  val jvmRuntimeArguments : Vector[String]  = ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.toVector
  val liveDebugging       : Boolean         = jvmRuntimeArguments.exists(_.contains("purpledebug"))
  val framesBufferable    : Int             = Math.min(24, (memoryFree - botRequiredDataSizeBytes) / JBWAPIClientDataSizeBytes).toInt
  val useFrameBuffer      : Boolean         = framesBufferable > 1 && ! liveDebugging

  val jbwapiConfiguration: BWClientConfiguration = new BWClientConfiguration()
    .withAutoContinue(true)
    .withMaxFrameDurationMs(40)
  jbwapiConfiguration.withAsyncFrameBufferCapacity(framesBufferable)
  if (useFrameBuffer) {
    jbwapiConfiguration.withAsync(true)
    jbwapiConfiguration.withAsyncUnsafe(true)
  }

  def main(args: Array[String]) {
    JBWAPIClient.startGame(jbwapiConfiguration)
  }
}
