package Lifecycle

import java.io.File

import scala.reflect.io.Path

class BwapiData {
  lazy val originalWorkingDirectory = System.getProperty("user.dir")
  lazy val intendedWorkingDirectory: String = getIntendedWorkingDirectory

  def isCorrect(path: String): Boolean = {
    val here = new File(path)
    val bwapiDataFile = here.listFiles().find(_.getName.toLowerCase == "bwapi-data")
    bwapiDataFile.exists(file =>
      Seq("ai", "read", "write").forall(subdirectory =>
        file.listFiles().exists(_.getName.toLowerCase == subdirectory)))
  }
  private def getIntendedWorkingDirectory: String = {
    try {
      // Try backing up until we find it
      var path = Path(originalWorkingDirectory)
      do {
        if (isCorrect(path.toString())) {
          return path.toString()
        }
        path = path.parent
      }
      while (path != path.parent)
      originalWorkingDirectory
    }
    catch {
      case exception: Exception => originalWorkingDirectory
    }
  }

  lazy val ai: String = intendedWorkingDirectory + "/bwapi-data/AI/"
  lazy val read: String = intendedWorkingDirectory + "/bwapi-data/read/"
  lazy val write: String = intendedWorkingDirectory + "/bwapi-data/write/"
}
