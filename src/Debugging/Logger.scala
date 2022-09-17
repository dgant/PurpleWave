package Debugging

import java.io.{File, PrintWriter}

import Lifecycle.With
import Utilities.Time.{Forever, Frames, Seconds}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Logger {
  
  private val logMessages = new ListBuffer[String]
  private val performanceMessages = new ListBuffer[String]
  private val microMessages = new ListBuffer[String]
  private var errorOcurred = false
  
  def flush(): Unit = {
    var opponents: String = ""
    try {
      opponents = With.enemies.map(_.name).mkString("-")
    } catch { case exception: Exception => }
    
    val filename = With.bwapiData.write + opponents + (if (errorOcurred) ".error" else ".normal") + ".log.txt"
    val file = new File(filename)
    val printWriter = new PrintWriter(file)
    printWriter.write(logMessages.mkString("\r\n"))
    printWriter.write(microMessages.mkString("\r\n"))
    printWriter.write(performanceMessages.mkString("\r\n"))
    printWriter.close()
  }

  private val lastInstance = new mutable.HashMap[String, Int]

  /**
    * Looks the same as the next method, except I set a debugging breakpoint in that one but not thihs one
    */
  def quietlyOnException(exception: Exception): Unit = {
    error("An exception was thrown on frame " + With.frame)
    error(formatException(exception))
  }
  
  def onException(exception: Exception): Unit = {
    quietlyOnException(exception)
  }

  def micro(message: String): Unit = {
    log(f"MICRO | $message", chat = false, target = microMessages)
  }
  
  def debug(message: String): Unit = {
    log(f"DEBUG | $message", chat = false)
  }
  
  def warn(message: String): Unit = {
    log(f"WARN  | $message", chat = false)
  }
  
  def error(message: String): Unit = {
    errorOcurred = true
    log(f"ERROR | $message")
  }

  def performance(message: String): Unit = {
    log(f"SPEED | $message", chat = false, logstd = false, target = performanceMessages)
  }

  private def canRelog(message: String, sleepFrames: Int): Boolean = {
    val output = lastInstance.getOrElse(message, -Forever()) < With.frame - sleepFrames
    lastInstance(message) = With.frame
    output
  }

  private val defaultSleepFrames = Seconds(15)()

  def debugCircuitBreaker(message: String, sleepFrames: Int = defaultSleepFrames): Unit = {
    if (canRelog(message, sleepFrames)) { debug(message) }
    lastInstance(message) = With.frame
  }

  def warnCircuitBreaker(message: String, sleepFrames: Int = defaultSleepFrames): Unit = {
    if (canRelog(message, sleepFrames)) { warn(message) }
  }

  def errorCircuitBreaker(message: String, sleepFrames: Int = defaultSleepFrames): Unit = {
    if (canRelog(message, sleepFrames)) { error(message) }
  }
  
  private def log(message: String, chat: Boolean = true, logstd: Boolean = true, target: mutable.Buffer[String] = logMessages): Unit = {
    val logMessage = f"${With.frame} | ${Frames(With.frame)} | $message"
    target.append(logMessage)
    if (With.configuration.logstd && logstd) {
      System.err.println(logMessage)
    }
    if (chat && With.configuration.debugging && With.manners != null) {
      With.manners.chat(message)
    }
  }
  
  private def formatException(exception: Exception): String = (
    f"${ToString(exception)}\n${exception.getMessage}\n${exception
      .getStackTrace
      .map(stackElement => f"${stackElement.getClassName}.${stackElement.getMethodName}(): ${stackElement.getLineNumber}")
      .mkString("\n")}")
}
