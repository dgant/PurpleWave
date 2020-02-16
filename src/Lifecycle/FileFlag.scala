package Lifecycle

import java.io.File

import scala.io.Source

class FileFlag(filename: String) {
  private lazy val fullPath: String = With.bwapiData.ai + filename
  private lazy val enabled: Boolean = {
    try {
      file.exists()
    }
    catch { case exception: Exception =>
      With.logger.warn("Exception looking for flag file at: " + fullPath)
      With.logger.onException(exception)
      false
    }
  }
  lazy val contents: String = {
    try {
      if (enabled) {
        Source.fromFile(filename).getLines.mkString("\n")
      } else ""
    } catch { case exception: Exception =>
      With.logger.warn("Exception reading file flag at: " + fullPath)
      With.logger.onException(exception)
      ""
    }
  }
  protected def file: File = new File(fullPath)
  def apply(): Boolean = enabled

  override def toString: String = "Flag: " + fullPath + (if (enabled) "(Enabled)" else "(Disabled)")
}
