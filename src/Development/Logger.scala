package Development

import java.io.{File, PrintWriter}
import java.util.Calendar

import Startup.With

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class Logger {
  val _logMessages = new ListBuffer[String]
  val _exceptions = new ListBuffer[Exception]
  
  def onEnd() {
    val opponents = With.game.getPlayers.asScala
      .filter(_ != With.game.self)
      .filter(_ != With.game.neutral)
      .map(_.getName)
      .mkString("-")
    
    val filenameRaw = (opponents + "-" + Calendar.getInstance.getTime.toString)
    val filename = "bwapi-data/write/" + filenameRaw.replaceAll("[^A-Za-z0-9 \\-\\.]", "") + ".log.txt";
    val file = new File(filename)
    System.out.println("Writing log to")
    System.out.println(file.getAbsolutePath)
    val printWriter = new PrintWriter(file)
    printWriter.write(exceptionLog)
    printWriter.close()
  }
  
  def exceptionLog:String = {
    _exceptions
        .map(_formatException)
        .distinct
        .mkString("\n")
  }
  
  def onException(exception: Exception) {
    _exceptions.append(exception)
    debug(_formatException(exception))
  }
  
  def _log(message:String) {
    System.out.println(message)
    _logMessages.append(message)
  }

  def debug(message:String) {
    _log(message)
  }
  
  def warn(message:String) {
    _log(message)
  }
  
  def error(message:String) {
    _log(message)
  }
  
  def _formatException(exception: Exception):String = {
    "EXCEPTION:\n" +
      "----------\n" +
      exception.getClass.getSimpleName +
      "\n" +
      exception.getMessage +
      exception.getStackTrace.foreach(stackElement => {
        stackElement.getClassName + "." +
        stackElement.getMethodName + "(): " +
        stackElement.getLineNumber
    })
  }
}
