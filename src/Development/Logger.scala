package Development

import java.io.{File, PrintWriter}
import java.util.Calendar

import Startup.With

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class Logger {
  
  val _logMessages = new ListBuffer[String]
  
  def flush() {
    val opponents = With.game.getPlayers.asScala
      .filter(_ != With.game.self)
      .filter(_ != With.game.neutral)
      .map(_.getName)
      .mkString("-")
    
    val filenameRaw = (opponents + "-" + Calendar.getInstance.getTime.toString)
    val filename = "bwapi-data/write/" + filenameRaw.replaceAll("[^A-Za-z0-9 \\-\\.]", "") + ".log.txt";
    val file = new File(filename)
    val printWriter = new PrintWriter(file)
    printWriter.write(_logMessages.distinct.mkString("\n"))
    printWriter.close()
  }
  
  def onException(exception: Exception) {
    _logMessages.append(_formatException(exception))
    debug(_formatException(exception))
  }
  
  def _log(message:String) {
    if (With.configuration.enableStdOut) {
      System.out.println(message)
    }
    
    if (With.configuration.enableChat) {
      With.game.sendText(message)
    }
    
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
      exception.getClass.getSimpleName + "\n" +
      exception.getMessage + "\n"
      exception.getStackTrace.map(stackElement => {
        stackElement.getClassName + "." +
        stackElement.getMethodName + "(): " +
        stackElement.getLineNumber
    }).mkString("\n")
  }
}
