package Debugging

import java.io.{File, PrintWriter}

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.{Manners, With}

import scala.collection.mutable.ListBuffer

class Logger {
  
  private val logMessages = new ListBuffer[String]
  private var errorOcurred = false
  
  def flush(): Unit = {
    val shouldFlush = errorOcurred || With.configuration.debugging

    if ( ! shouldFlush) return

    var opponents: String = ""
    try {
      opponents = With.enemies.map(_.name).mkString("-")
    } catch { case exception: Exception => }
    
    val filename = With.bwapiData.write + ".log.txt"
    val file = new File(filename)
    val printWriter = new PrintWriter(file)
    printWriter.write(logMessages.mkString("\r\n"))
    printWriter.close()
  }
  
  def onException(exception: Exception) {
    error("An exception was thrown on frame " + With.frame)
    error(formatException(exception))
  }
  
  def debug(message: String) {
    log("DEBUG | " + message, chat = false)
  }
  
  def warn(message: String) {
    log("WARN  | " + message)
  }
  
  def error(message: String) {
    errorOcurred = true
    log("ERROR | " + message)
  }
  
  private def log(message: String, chat: Boolean = true) {
    val logMessage = With.frame.toString + " | " + new GameTime(With.frame).toString + " | " + message
    logMessages.append(logMessage)
    if (With.configuration.logstd) {
      System.err.println(logMessage)
    }
    if (chat && With.configuration.debugging) {
      Manners.chat(message)
    }
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
