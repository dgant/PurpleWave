package Lifecycle

import java.io.File

import scala.reflect.io.Path

class BwapiData {
  lazy val workingDirectoryDeFacto = System.getProperty("user.dir")
  lazy val workingDirectoryDeJure: String = getWorkingDirectoryDeJure
  lazy val ai: String = workingDirectoryDeJure + "/bwapi-data/AI/"
  lazy val read: String = workingDirectoryDeJure + "/bwapi-data/read/"
  lazy val write: String = workingDirectoryDeJure + "/bwapi-data/write/"

  def isCorrect(path: String): Boolean = {
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
    }
    catch {
      case exception: Exception => workingDirectoryDeFacto
    }
  }
}
