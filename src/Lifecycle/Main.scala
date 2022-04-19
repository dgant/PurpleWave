package Lifecycle

import bwapi.BWClientConfiguration

import java.lang.management.ManagementFactory
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
    .withAsyncFrameBufferCapacity(framesBufferable)
    .withAsync(useFrameBuffer)
    .withAsyncUnsafe(useFrameBuffer)

  def main(args: Array[String]): Unit = {
    PurpleBWClient.startGame(jbwapiConfiguration)
  }
}
