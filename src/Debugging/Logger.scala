package Debugging

import java.io.{File, PrintWriter}
import java.util.Calendar

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.{Manners, With}

import scala.collection.mutable.ListBuffer

class Logger {
  
  private val logMessages = new ListBuffer[String]
  private var errorOcurred = false
  
  def flush(): Unit = {
    var shouldFlush = true
    var opponents: String = ""
    try {
      if ( ! errorOcurred && ! With.configuration.debugging) {
        shouldFlush = false
      }
      opponents = With.enemies.map(_.name).mkString("-")
    } catch { case exception: Exception => {} }

    if ( ! shouldFlush) return
    
    val filenameRaw = opponents + "-" + Calendar.getInstance.getTime.toString
    val filename = With.bwapiData.write + filenameRaw.replaceAll("[^A-Za-z0-9 \\-\\.]", "") + ".log.txt"
    val file = new File(filename)
    val printWriter = new PrintWriter(file)
    printWriter.write(logMessages.distinct.mkString("\r\n"))
    printWriter.close()
  }
  
  def onException(exception: Exception) {
    errorOcurred = true
    log("An exception was thrown on frame " + With.frame)
    logMessages.append(formatException(exception))
    debug(formatException(exception))
  }
  
  def debug(message: String) {
    log(message, chat = false)
  }
  
  def warn(message: String) {
    errorOcurred = true
    log(message)
  }
  
  def error(message: String) {
    errorOcurred = true
    log(message)
  }
  
  private def log(message: String, chat: Boolean = true) {
    var logMessage = With.frame + " | " + new GameTime(With.frame).toString + " | " + message
    logMessages.append(logMessage)
    System.err.println(logMessage)
    if (chat && With.configuration.debugging) Manners.chat(message)
  }
  
  private def formatException(exception: Exception): String = {
    exception.getClass.getSimpleName + "\n" +
    exception.getMessage + "\n" +
    exception.getStackTrace.map(stackElement => {
      stackElement.getClassName + "." +
      stackElement.getMethodName + "(): " +
      stackElement.getLineNumber
    }).mkString("\n")
  }
}
