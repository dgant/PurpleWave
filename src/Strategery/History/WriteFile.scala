package Strategery.History

import java.io.{BufferedWriter, File, FileWriter}

import Lifecycle.With

object WriteFile {

  def apply(filename: String, lines: Iterable[String], contextDescription: String) {

    var bufferedWriter: BufferedWriter = null

    try {
      val file            = new File(filename)
      val fileWriter      = new FileWriter(file)
          bufferedWriter  = new BufferedWriter(fileWriter)

      lines.map(_ + "\n").foreach(bufferedWriter.write)
    }
    catch { case exception: Exception =>
      With.logger.warn("Failed to " + contextDescription + " to " + filename)
      With.logger.onException(exception)
    }
    if (bufferedWriter != null) {
      bufferedWriter.close()
    }
  }
}
