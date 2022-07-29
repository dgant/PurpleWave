package Lifecycle.Configure

import Lifecycle.With

import java.io.{File, PrintWriter}
import java.nio.file.Paths
import scala.reflect.io.Path

class BwapiData {
  lazy val workingDirectoryDeFacto  : String = System.getProperty("user.dir")
  lazy val workingDirectoryDeJure   : String = getWorkingDirectoryDeJure
  lazy val ai     : String = workingDirectoryDeJure + "/bwapi-data/AI/"
  lazy val read   : String = workingDirectoryDeJure + "/bwapi-data/read/"
  lazy val write  : String = workingDirectoryDeJure + "/bwapi-data/write/"
  lazy val directoriesARW: Vector[String] = Vector(ai, read, write)

  private def isCorrect(path: String): Boolean = {
    val here = new File(path)
    val bwapiDataFile = here.listFiles().find(_.getName.toLowerCase == "bwapi-data")
    bwapiDataFile.exists(file =>
      Seq("ai", "read", "write").forall(subdirectory =>
        file.listFiles().exists(_.getName.toLowerCase == subdirectory)))
  }

  private def getWorkingDirectoryDeJure: String = {
    try {
      // Try backing up until we find it
      var path = Path(workingDirectoryDeFacto)
      do {
        if (isCorrect(path.toString())) {
          return path.toString()
        }
        path = path.parent
      }
      while (path != path.parent)
      workingDirectoryDeFacto
    } catch {
      case exception: Exception => workingDirectoryDeFacto
    }
  }

  def writeToFile(filename: String, text: String): Unit = {
    try {
      val file = Paths.get(With.bwapiData.write, filename).toFile
      val writer = new PrintWriter(file)
      writer.print(text)
      writer.close()
    } catch { case exception: Exception => With.logger.onException(exception) }
  }

  def readFromARW(filename: String): Option[File] = {
    directoriesARW.foreach(directory => {
      try {
        val output = Paths.get(directory, filename).toFile
        if (output.exists()) {
          With.logger.debug(f"Found ${output.getAbsoluteFile}")
          return Some(output)
        }
      } catch { case exception: Exception => With.logger.onException(exception) }
    })
    None
  }

  def readFromARWToString(filename: String): Option[String] = {
    readFromARW(filename).flatMap(file =>
      try {
        Some(scala.io.Source.fromFile(file).mkString)
      } catch { case exception: Exception =>
        With.logger.onException(exception)
        None
      })
  }
}
