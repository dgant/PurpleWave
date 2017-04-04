package Debugging

import java.io.{File, PrintWriter}
import java.util.Calendar

import Lifecycle.With

import scala.collection.mutable.ListBuffer

class Logger {
  
  private val logMessages = new ListBuffer[String]
  
  def flush() {
    val opponents = With.enemies
      .map(_.getName)
      .mkString("-")
    
    val filenameRaw = (opponents + "-" + Calendar.getInstance.getTime.toString)
    val filename = "bwapi-data/write/" + filenameRaw.replaceAll("[^A-Za-z0-9 \\-\\.]", "") + ".log.txt";
    val file = new File(filename)
    val printWriter = new PrintWriter(file)
    printWriter.write(logMessages.distinct.mkString("\n"))
    printWriter.close()
  }
  
  def onException(exception: Exception) {
    logMessages.append(formatException(exception))
    debug(formatException(exception))
  }
  
  def debug(message:String) {
    log(message)
  }
  
  def warn(message:String) {
    log(message)
  }
  
  def error(message:String) {
    log(message)
  }
  
  private def log(message:String) {
    if (With.configuration.enableStdOut) {
      System.out.println(message)
    }
    
    if (With.configuration.enableChat) {
      With.game.sendText(message)
    }
    
    logMessages.append(message)
  }
  
  private def formatException(exception: Exception):String = {
      exception.getClass.getSimpleName + "\n" +
      exception.getMessage + "\n"
      exception.getStackTrace.map(stackElement => {
        stackElement.getClassName + "." +
        stackElement.getMethodName + "(): " +
        stackElement.getLineNumber
    }).mkString("\n")
  }
}
